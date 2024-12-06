import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import org.jocl.*;

import static org.jocl.CL.*;

public class WordCountMain {
    private static final int CL_SUCCESS = 0;

    public static void main(String[] args) throws Exception {
        String filePath = "DonQuixote-388208.txt"; // Caminho do arquivo de entrada
        String word = "quijote";       // Palavra a ser contada
        int numExecutions = 3;         // Número de execuções por teste

        // Lista para armazenar resultados para o gráfico e CSV
        List<String[]> results = new ArrayList<>();
        results.add(new String[]{"Método", "TamanhoTexto", "TempoExecucao"});

        System.out.println("Resultados:");

        // SerialCPU
        for (int i = 0; i < numExecutions; i++) {
            long startTime = System.nanoTime();
            int count = countWordsSerial(filePath, word);
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000; // Tempo em ms
            System.out.printf("SerialCPU: %d ocorrências em %d ms%n", count, executionTime);
            results.add(new String[]{"SerialCPU", getFileSize(filePath), String.valueOf(executionTime)});
        }

        // ParallelCPU
        for (int threads = 1; threads <= Runtime.getRuntime().availableProcessors(); threads *= 2) {
            for (int i = 0; i < numExecutions; i++) {
                long startTime = System.nanoTime();
                int count = countWordsParallelCPU(filePath, word, threads);
                long endTime = System.nanoTime();
                long executionTime = (endTime - startTime) / 1_000_000;
                System.out.printf("ParallelCPU (%d threads): %d ocorrências em %d ms%n", threads, count, executionTime);
                results.add(new String[]{"ParallelCPU-" + threads, getFileSize(filePath), String.valueOf(executionTime)});
            }
        }

        // ParallelGPU
        for (int i = 0; i < numExecutions; i++) {
            long startTime = System.nanoTime();
            int count = countWordsParallelGPU(filePath, word);
            long endTime = System.nanoTime();
            long executionTime = (endTime - startTime) / 1_000_000;
            System.out.printf("ParallelGPU: %d ocorrências em %d ms%n", count, executionTime);
            results.add(new String[]{"ParallelGPU", getFileSize(filePath), String.valueOf(executionTime)});
        }

        // Gerar arquivo CSV
        writeCsv("results.csv", results);

        // Gerar gráfico
        generateChart("Tempo de Execução de Algoritmos", "Método e Configuração", "Tempo (ms)", results);
    }

    // Método Serial
    public static int countWordsSerial(String filePath, String word) throws IOException {
        String content = Files.readString(Paths.get(filePath)).toLowerCase();
        String[] words = content.split("\\W+");
        int count = 0;
        for (String w : words) {
            if (w.equals(word)) {
                count++;
            }
        }
        return count;
    }

    // Método Paralelo na CPU
    public static int countWordsParallelCPU(String filePath, String word, int numThreads) throws IOException, InterruptedException {
        String content = Files.readString(Paths.get(filePath)).toLowerCase();
        String[] words = content.split("\\W+");
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        List<Future<Integer>> results = new ArrayList<>();
        int chunkSize = (int) Math.ceil(words.length / (double) numThreads);

        for (int i = 0; i < numThreads; i++) {
            final int start = i * chunkSize;
            final int end = Math.min(start + chunkSize, words.length);
            results.add(executor.submit(() -> {
                int count = 0;
                for (int j = start; j < end; j++) {
                    if (words[j].equals(word)) {
                        count++;
                    }
                }
                return count;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int total = 0;
        for (Future<Integer> result : results) {
            try {
                total += result.get();
            } catch (ExecutionException e) {
                System.err.println("Erro ao executar uma thread: " + e.getCause());
            }
        }
        return total;
    }

    // Método Paralelo na GPU com JOCL corrigido
    public static int countWordsParallelGPU(String filePath, String word) throws IOException {
        String content = Files.readString(Paths.get(filePath)).toLowerCase();
        byte[] textBytes = content.getBytes();
        byte[] wordBytes = word.getBytes();

        // Configuração do OpenCL
        CL.setExceptionsEnabled(true);
        cl_platform_id[] platforms = new cl_platform_id[1];
        clGetPlatformIDs(1, platforms, null);
        cl_platform_id platform = platforms[0];

        cl_device_id[] devices = new cl_device_id[1];
        clGetDeviceIDs(platform, CL_DEVICE_TYPE_GPU, 1, devices, null);
        cl_device_id device = devices[0];

        cl_context context = clCreateContext(null, 1, new cl_device_id[]{device}, null, null, null);
        cl_command_queue queue = clCreateCommandQueue(context, device, 0, null);

        // Fonte do programa OpenCL
        String programSource = """
            // Função auxiliar para verificar se é alfanumérico
            int isalnum(char c) {
                return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9');
            }
        
            __kernel void countWords(__global const char* text, 
                                     __global const char* word, 
                                     const int textLength, 
                                     const int wordLength, 
                                     __global int* result) {
                int id = get_global_id(0);
                
                // Verifica se está fora do intervalo válido
                if (id + wordLength > textLength) {
                    return;
                }
        
                // Verifica se a palavra é igual no local atual
                int match = 1;
                for (int i = 0; i < wordLength; i++) {
                    if (text[id + i] != word[i]) {
                        match = 0;
                        break;
                    }
                }
        
                // Verifica os delimitadores para garantir que seja uma palavra completa
                if (match == 1) {
                    // Delimitador antes da palavra
                    if (id > 0 && isalnum(text[id - 1])) {
                        match = 0;
                    }
        
                    // Delimitador depois da palavra
                    if (id + wordLength < textLength && isalnum(text[id + wordLength])) {
                        match = 0;
                    }
                }
        
                // Incrementa o resultado se for uma correspondência válida
                if (match == 1) {
                    atomic_add(result, 1);
                }
            }
        """;


        cl_program program = clCreateProgramWithSource(context, 1, new String[]{programSource}, null, null);
        clBuildProgram(program, 0, null, null, null, null);

        cl_kernel kernel = clCreateKernel(program, "countWords", null);

        // Alocar buffers
        cl_mem textBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * textBytes.length, Pointer.to(textBytes), null);
        cl_mem wordBuffer = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_char * wordBytes.length, Pointer.to(wordBytes), null);
        cl_mem resultBuffer = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_int, null, null);

        // Inicializar resultado
        int[] resultArray = new int[1];
        resultArray[0] = 0;

        // Configurar argumentos do kernel
        clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(textBuffer));
        clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(wordBuffer));
        clSetKernelArg(kernel, 2, Sizeof.cl_int, Pointer.to(new int[]{textBytes.length}));
        clSetKernelArg(kernel, 3, Sizeof.cl_int, Pointer.to(new int[]{wordBytes.length}));
        clSetKernelArg(kernel, 4, Sizeof.cl_mem, Pointer.to(resultBuffer));

        // Executar kernel
        long[] globalWorkSize = new long[]{textBytes.length};
        clEnqueueNDRangeKernel(queue, kernel, 1, null, globalWorkSize, null, 0, null, null);

        // Ler resultado da memória da GPU
        clEnqueueReadBuffer(queue, resultBuffer, CL_TRUE, 0, Sizeof.cl_int, Pointer.to(resultArray), 0, null, null);

        // Limpar recursos
        clReleaseMemObject(textBuffer);
        clReleaseMemObject(wordBuffer);
        clReleaseMemObject(resultBuffer);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(queue);
        clReleaseContext(context);

        return resultArray[0];
    }

    // Obter o tamanho do arquivo em KB
    private static String getFileSize(String filePath) throws IOException {
        long size = Files.size(Paths.get(filePath));
        return size / 1024 + "KB";
    }

    // Gerar Gráfico com JFreeChart
    public static void generateChart(String title, String xLabel, String yLabel, List<String[]> results) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (String[] result : results) {
            if (!result[0].equals("Método")) {
                dataset.addValue(Double.parseDouble(result[2]), result[0], result[1]);
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(title, xLabel, yLabel, dataset, PlotOrientation.VERTICAL, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(chartPanel);
        frame.pack();
        frame.setVisible(true);
    }

    // Gerar Arquivo CSV
    public static void writeCsv(String fileName, List<String[]> results) throws IOException {
        try (PrintWriter writer = new PrintWriter(new File(fileName))) {
            for (String[] row : results) {
                writer.println(String.join(",", row));
            }
        }
    }
}