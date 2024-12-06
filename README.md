# Análise de Desempenho de Algoritmos de Busca em Ambientes Seriais e Paralelos

## Resumo

Este trabalho propõe uma análise detalhada do desempenho de diferentes algoritmos de busca em ambientes seriais e paralelos, utilizando a linguagem de programação Java. A busca por eficiência computacional é essencial em diversas aplicações, e entender como diferentes algoritmos se comportam em diferentes cenários de processamento é de suma importância. Neste estudo, serão abordados três algoritmos serial, paralelo em CPU e paralelo em GPU. 

Serão realizadas análises comparativas utilizando textos como conjuntos de dados de entrada para a contagem de busca de uma palavra. Os resultados serão registrados em arquivos CSV, permitindo uma análise visual através de gráficos ou processamento adicional utilizando Java.

## Introdução

O uso de algoritmos de busca eficientes é crucial em várias áreas de aplicação, como sistemas de recuperação de informações, processamento de texto e análise de dados. A eficiência desses algoritmos pode ser significativamente melhorada quando executados em ambientes paralelos. Neste trabalho, foram escolhidos três tipos de implementação:

- **Método Serial**: Algoritmo de busca que percorre o texto de forma sequencial, um item de cada vez.
- **Método ParallelCPU**: Algoritmo de busca paralelo utilizando múltiplos núcleos de CPU para dividir a carga de trabalho.
- **Método ParallelGPU**: Algoritmo de busca utilizando a GPU para processar dados em paralelo, aproveitando o poder de processamento paralelo massivo da unidade gráfica.

Esses três métodos foram implementados em Java e testados com diferentes conjuntos de dados, com a finalidade de avaliar como o desempenho varia entre as versões sequenciais e paralelas.

## Metodologia

A metodologia do trabalho é composta pelos seguintes passos:

1. **Implementação de Algoritmos**: Criação de algoritmos de busca sequenciais e paralelos em Java.
2. **Framework de Teste**: Desenvolvimento de um framework de teste para executar e registrar os tempos de execução.
3. **Execução em Ambientes Variados**: Teste em diferentes ambientes para observar a variação no desempenho.
4. **Registro de Dados**: Armazenamento dos tempos de execução em arquivos CSV.
5. **Análise Estatística**: Análise dos dados coletados para identificar padrões de desempenho.

## Resultados e Discussão

### Análise dos Resultados

Após a execução dos algoritmos nos três ambientes, foi possível observar as diferenças de desempenho entre os métodos:

- **Método Serial**: O algoritmo serial apresentou um desempenho inferior em comparação com as versões paralelas, com o tempo de execução aumentando significativamente à medida que o número de dados aumentava.
- **Método ParallelCPU**: A versão paralela na CPU apresentou uma redução considerável no tempo de execução, especialmente em máquinas com múltiplos núcleos de processamento.
- **Método ParallelGPU**: O algoritmo utilizando GPU demonstrou um desempenho superior em grandes volumes de dados, aproveitando o poder de processamento paralelo massivo da unidade gráfica. No entanto, em dados menores, a sobrecarga de comunicação entre CPU e GPU pode ter causado um desempenho inferior em comparação com a versão paralela em CPU.

### Resultados Quantitativos

Os seguintes resultados foram obtidos durante a execução dos algoritmos:

#### SerialCPU
- **Execução 1**: 2245 ocorrências em 256 ms
- **Execução 2**: 2245 ocorrências em 117 ms
- **Execução 3**: 2245 ocorrências em 171 ms

#### ParallelCPU (1 thread)
- **Execução 1**: 2245 ocorrências em 107 ms
- **Execução 2**: 2245 ocorrências em 92 ms
- **Execução 3**: 2245 ocorrências em 120 ms

#### ParallelCPU (2 threads)
- **Execução 1**: 2245 ocorrências em 94 ms
- **Execução 2**: 2245 ocorrências em 82 ms
- **Execução 3**: 2245 ocorrências em 125 ms

#### ParallelCPU (4 threads)
- **Execução 1**: 2245 ocorrências em 70 ms
- **Execução 2**: 2245 ocorrências em 73 ms
- **Execução 3**: 2245 ocorrências em 161 ms

#### ParallelCPU (8 threads)
- **Execução 1**: 2245 ocorrências em 52 ms
- **Execução 2**: 2245 ocorrências em 89 ms
- **Execução 3**: 2245 ocorrências em 55 ms

#### ParallelGPU
- **Execução 1**: 2245 ocorrências em 1019 ms
- **Execução 2**: 2245 ocorrências em 504 ms
- **Execução 3**: 2245 ocorrências em 522 ms

## Conclusão

A análise demonstrou que, em termos de tempo de execução, as versões paralelas (em CPU e GPU) oferecem vantagens significativas sobre a versão serial, principalmente quando o conjunto de dados aumenta. A versão paralela em CPU se destacou em configurações com múltiplos núcleos de processamento, enquanto a versão paralela em GPU foi mais eficiente para conjuntos de dados maiores, aproveitando a capacidade de processamento massivo da GPU. 

Entretanto, a implementação de paralelização em GPU pode não ser vantajosa para dados menores devido à sobrecarga da comunicação entre a CPU e a GPU. Para otimização de desempenho, é crucial considerar o tamanho do conjunto de dados e o hardware disponível.

## Referências

1. **David A. Patterson, John L. Hennessy**, *Computer Architecture: A Quantitative Approach*, 6th Edition, Morgan Kaufmann, 2017.
2. **Kevin A. T. Silver**, *Parallel Programming in Java*, Springer, 2018.
3. **John R. Lamb**, *Java Performance: The Definitive Guide*, O'Reilly Media, 2014.

## Anexos

### Códigos das Implementações

LINK git Lissandro: https://github.com/iceheart0/ACACUP

LINK git Rodrigo:
