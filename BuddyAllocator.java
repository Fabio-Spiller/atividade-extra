// Importações de I/O permitidas (código de suporte)
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException; 

/**
 * BuddyAllocator: Implementa o Alocador de Memória Buddy Binário.
 */
public class BuddyAllocator {

    // --- Constantes de Configuração da Memória ---
    private final int MEMORIA_TOTAL = 4194304; 
    private final int MIN_BLOCK_SIZE_BYTES = 1024; 
    
    // Níveis logarítmicos
    private final int MAX_LEVEL = 22; 
    private final int MIN_LEVEL = 10; 
    
    private final int TOTAL_NIVEIS = MAX_LEVEL - MIN_LEVEL + 1; 
    private final int TOTAL_SLOTS = MEMORIA_TOTAL / MIN_BLOCK_SIZE_BYTES; 
    
    // Códigos de Status
    private final int LIVRE = 0;
    private final int OCUPADO = 1;
    private final int NULO = -1; 

    // --- Estruturas de Dados Primárias (Arrays Paralelos) ---
    private int[] freeHeads; 
    private int[] level; 
    private int[] status; 
    private char[] label; 
    private int[] realSize; 
    private int[] nextPointer; 
    
    // Variáveis auxiliares de controle de tamanho
    private final int freeHeads_size = TOTAL_NIVEIS;
    private final int slotArrays_size = TOTAL_SLOTS;


    /**
     * Construtor
     */
    public BuddyAllocator() {
        this.freeHeads = new int[TOTAL_NIVEIS];
        this.level = new int[TOTAL_SLOTS];
        this.status = new int[TOTAL_SLOTS];
        this.label = new char[TOTAL_SLOTS];
        this.realSize = new int[TOTAL_SLOTS];
        this.nextPointer = new int[TOTAL_SLOTS];

        int i = 0;
        while (i < freeHeads_size) {
            this.freeHeads[i] = NULO;
            i = i + 1;
        }
        
        i = 0;
        while (i < slotArrays_size) {
            this.level[i] = MIN_LEVEL;
            this.status[i] = LIVRE;
            this.label[i] = ' ';
            this.realSize[i] = 0;
            this.nextPointer[i] = NULO;
            i = i + 1;
        }

        this.level[0] = MAX_LEVEL;
        adicionarBlocoLivre(0, MAX_LEVEL);

        System.out.println("Buddy Allocator Inicializado.");
        System.out.println("Memória Total Gerenciada: " + (MEMORIA_TOTAL / 1024) + " KB.");
        System.out.println("--------------------------------------------------");
    }
    
    /**
     * Calcula o valor de 2^(nivel - MIN_LEVEL).
     */
    private int obterPotenciaDeDoisKB(int nivelAtual) {
        int expoente = nivelAtual - MIN_LEVEL;
        int resultado = 1;
        int i = 0;
        while (i < expoente) {
            resultado = resultado * 2;
            i = i + 1;
        }
        return resultado;
    }
    
    /**
     * Calcula o menor Nível que comporta o tamanho solicitado.
     */
    private int calcularNivel(int tamanhoKB) {
        int tamanhoBytes = tamanhoKB * 1024;
        int nivelAtual = MIN_LEVEL;
        int tamanhoAtual = MIN_BLOCK_SIZE_BYTES; 
        
        while (tamanhoAtual < tamanhoBytes) {
            tamanhoAtual = tamanhoAtual * 2;
            nivelAtual = nivelAtual + 1;
            if (nivelAtual > MAX_LEVEL) {
                return MAX_LEVEL;
            }
        }
        return nivelAtual;
    }
    
    /**
     * Mapeia o nível do bloco para o índice do array freeHeads.
     */
    private int obterIndiceListaLivre(int level) {
        return level - MIN_LEVEL;
    }
    
    /**
     * Adiciona um bloco livre no INÍCIO da Free List correspondente (LIFO).
     */
    private void adicionarBlocoLivre(int slotIndex, int level) {
        int listIndex = obterIndiceListaLivre(level);
        int indiceCabeca = this.freeHeads[listIndex];
        
        this.status[slotIndex] = LIVRE;
        
        this.nextPointer[slotIndex] = indiceCabeca;
        
        this.freeHeads[listIndex] = slotIndex;
    }
    
    /**
     * Remove o bloco que está na CABEÇA (HEAD) da Free List.
     */
    private void removerBlocoCabeca(int slotIndex, int level) {
        int listIndex = obterIndiceListaLivre(level);
        int indiceProximo = this.nextPointer[slotIndex];
        
        this.freeHeads[listIndex] = indiceProximo;
        
        this.nextPointer[slotIndex] = NULO;
    }

    /**
     * Tenta alocar a memória.
     */
    public boolean alocar(int tamanhoKB, char label) {
        int targetLevel = calcularNivel(tamanhoKB);
        int targetListIndex = obterIndiceListaLivre(targetLevel);
        
        // 1. Busca pelo bloco livre adequado (First Fit adaptado)
        int slotEncontrado = NULO;
        int nivelEncontrado = NULO;
        int listIndex = targetListIndex;
        
        while (listIndex < TOTAL_NIVEIS) {
            if (this.freeHeads[listIndex] != NULO) {
                slotEncontrado = this.freeHeads[listIndex]; 
                nivelEncontrado = MIN_LEVEL + listIndex;
                break;
            }
            listIndex = listIndex + 1;
        }
        
        if (slotEncontrado == NULO) {
            System.out.println("[FALHA] Programa " + label + " (" + tamanhoKB + " KB): Sem bloco livre adequado.");
            return false;
        }
        
        // 2. Remove o bloco encontrado da Free List
        removerBlocoCabeca(slotEncontrado, nivelEncontrado);
        
        // 3. Divisão (Buddy Splitting)
        int nivelAtual = nivelEncontrado;
        int slotAtual = slotEncontrado;
        
        while (nivelAtual > targetLevel) {
            nivelAtual = nivelAtual - 1; 
            
            int blockSizeSlots = obterPotenciaDeDoisKB(nivelAtual);
            
            int buddySlot = slotAtual + blockSizeSlots;
            
            this.level[slotAtual] = nivelAtual;
            
            this.level[buddySlot] = nivelAtual;
            adicionarBlocoLivre(buddySlot, nivelAtual); 
        }
        
        // 4. Alocação final
        this.status[slotAtual] = OCUPADO;
        this.label[slotAtual] = label;
        this.realSize[slotAtual] = tamanhoKB;
        
        int blocoAlocadoKB = obterPotenciaDeDoisKB(targetLevel);
        System.out.println("[ALOCADO] Programa " + label + " (" + tamanhoKB + " KB). Bloco: " + blocoAlocadoKB + " KB. Slot: " + slotAtual);
        return true;
    }
    
    /**
     * Imprime o relatório final da memória.
     */
    public void gerarRelatorio() {
        System.out.println("\n==================================================");
        System.out.println("                 RELATÓRIO FINAL                  ");
        System.out.println("==================================================");

        int totalLivreKB = 0;
        int contagemFragmentosLivres = 0;
        
        // 1. Lista de Blocos Ocupados
        System.out.println("\n[1] Blocos Alocados:");
        System.out.println("--------------------------------------------------");
        System.out.println("Rótulo | Tam. Real (KB) | Tam. Bloco (KB) | Slot Inicial");
        System.out.println("--------------------------------------------------");
        
        int i = 0;
        while (i < slotArrays_size) {
            int nivelAtual = this.level[i];
            int tamanhoAtualKB = obterPotenciaDeDoisKB(nivelAtual);
            int blockSizeSlots = obterPotenciaDeDoisKB(nivelAtual);

            if (this.status[i] == OCUPADO) {
                // Bloco Alocado
                System.out.printf("%-6c | %-14d | %-15d | %-12d\n", 
                    this.label[i], 
                    this.realSize[i], 
                    tamanhoAtualKB, 
                    i);
            } else if (this.status[i] == LIVRE) {
                // Bloco Livre
                totalLivreKB = totalLivreKB + tamanhoAtualKB;
                contagemFragmentosLivres = contagemFragmentosLivres + 1;
            }
            
            i = i + blockSizeSlots;
        }
        System.out.println("--------------------------------------------------");

        // 2. Resumo da Memória Livre
        System.out.println("\n[2] Resumo da Memória Livre:");
        System.out.println("Área Total Livre: " + totalLivreKB + " KB");
        
        // 3. Fragmentação (Contagem de Blocos Livres)
        System.out.println("Quantidade de Blocos Livres (Fragmentos): " + contagemFragmentosLivres);
        
        // Detalhes dos fragmentos (mostrando as Free Lists)
        System.out.println("\n[3] Detalhes dos Fragmentos Livres (por Free List):");
        int listIndex = 0;
        while (listIndex < TOTAL_NIVEIS) {
            int level = MIN_LEVEL + listIndex;
            int sizeKB = obterPotenciaDeDoisKB(level);
            int head = this.freeHeads[listIndex];
            int count = 0;
            
            System.out.printf("  Nível %d (%d KB): ", level, sizeKB);
            
            int atual = head;
            while (atual != NULO) {
                System.out.print("[" + atual + "] ");
                atual = this.nextPointer[atual]; 
                count = count + 1;
            }
            
            System.out.println(" (Total: " + count + ")");
            listIndex = listIndex + 1;
        }

        System.out.println("==================================================");
    }

    /**
     * Função de suporte para ler o arquivo de entrada. 
     */
    public void executarDoArquivo(String nomeArquivo) {
        System.out.println("Iniciando alocação de programas a partir do arquivo: " + nomeArquivo);
        
        try {
            File file = new File(nomeArquivo);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String linha = bufferedReader.readLine();
            
            while (linha != null) {
                int indiceEspaco = NULO;
                int k = 0;
                while (k < 100) { 
                    if (k < linha.length() && linha.charAt(k) == ' ') {
                        indiceEspaco = k;
                        break;
                    }
                    k = k + 1;
                }

                if (indiceEspaco > 0) {
                    char label = linha.charAt(0);
                    String tamanhoStr = linha.substring(indiceEspaco + 1);
                    int tamanhoKB = Integer.parseInt(tamanhoStr);
                    
                    alocar(tamanhoKB, label);
                }

                linha = bufferedReader.readLine();
            }

            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("ERRO de Leitura/Escrita: Não foi possível ler o arquivo '" + nomeArquivo + "'. Detalhe: " + e.getMessage());
        } catch (NumberFormatException e) {
             System.out.println("ERRO de Formato: O arquivo contém um tamanho inválido que não é um número inteiro. Detalhe: " + e.getMessage());
        }
    }

    /**
     * Função principal para execução.
     */
    public static void main(String[] args) {
        BuddyAllocator alocador = new BuddyAllocator();
        String arquivoEntrada = "ProgramaEntrada.txt";

        alocador.executarDoArquivo(arquivoEntrada);

        alocador.gerarRelatorio();
    }
}