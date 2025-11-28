Alocador de Memória Buddy Binário em Java (Simulação

Ao clonar essa pasta o BuddyAllocator.java e o ProgramaEntrada.txt vão estar no mesmo local.

Rode o código java na sua IDE, e ele deve puxar os dados do ProgramaEntrada.txt automaticamente.

O resultado será exibido diretamente no terminal.

O arquivo de entrada é onde listamos os programas que a memória deve alocar.Cada linha deve conter o rótulo do programa e o numero que simula o tamanho separados por um espaço.
Exemplo:

A 2048
B 1024
T 3

Rótulo (A, B, T): É o nome do programa.
Tamanho (2048, 1024, 3): O quanto de memória o programa está pedindo.

Estrutura de Dados e Representação dos BlocosSlots de Memória: A memória total de 4 MB foi dividida em 4096 partes no caso slotes, onde cada parte representa 1 KB que é a unidade mínima pedida.

Em vez de classes, usamos vários arrays simples. O índice de cada array (i) representa o Slot na memória. Por exemplo, status[i] guarda o estado LIVRE/OCUPADO do bloco que começa no Slot i.

Usamos o level ou nível para definir o tamanho de um bloco como uma potência de 2. O Nível 10 é 1 KB, o Nível 12 é 4 KB, e assim por diante.

Gerenciamento de Blocos Livres freeHeads: Este array é o nosso catálogo, com 13 posições, uma para cada tamanho de bloco. Ele aponta para o primeiro bloco livre de cada tamanhoe o nextPointer simula um ponteiro. Ele conecta todos os blocos livres do mesmo tamanho em uma fila. Se um bloco de 512 KB está no Slot 500, o nextPointer[500] diz qual é o Slot do próximo bloco livre de 512 KB

AlocaçãoFirst Fit: Quando um programa pede memória, o código primeiro calcula o menor bloco de potência de 2 que ele precisa. Em seguida, ele procura na lista livre.Se não houver o tamanho exato, ele pega o primeiro bloco maior que encontrar.Esse bloco maior é então picado até atingir o tamanho necessário. A metade não utilizada em cada divisão volta pra fila de blocos livres.

Fizemos uma lista mais didatica de programas pra entender mais facil a alocação, que com numeros quebrados seria mais chato. Funciona mesmo assim
