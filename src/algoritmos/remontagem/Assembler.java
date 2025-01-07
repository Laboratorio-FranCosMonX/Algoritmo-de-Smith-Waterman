package algoritmos.remontagem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import algoritmos.smithWaterman.arquivo.Arquivo;

public class Assembler {
	private static Scanner sc = new Scanner(System.in);
//	private static Integer k;
	private Kmer.Aresta [] [] mat;
	private List<Kmer> itens; //pode ser usado tanto para comparar linha quanto coluna
//	private static Integer qtd_arestas = 0; //ver se todos os vertices estao conectados
	private static Integer qtd_ja_montado = 0;
	private static String resposta = "";
	
	public Assembler(String [] itens, int tamanho) {
		intro(itens, tamanho);
		montarMatriz();
		System.out.println("Montagem da matriz " + tamanho + "x" + tamanho + " concluído com exito.");
		System.out.println("Comecando a alimentar a matriz.");
		
		int inicia_em = descubridor();
		if(inicia_em == -1) {
			System.err.println("Fim do programa de forma negativa");
			return;
		}
		System.out.println("Alimentacao concluida com exito.");
		
		System.out.println("Construindo a solucao.");
		rastreador(inicia_em);
	}
	
	private void intro(String [] itens, int tamanho) {
		this.itens = new ArrayList<Kmer>();
		for(String s : itens) {
			if(s != "")
				this.itens.add(new Kmer(s));
		}
		this.mat = new Kmer.Aresta[tamanho][tamanho];
		for(int i = 0 ; i < this.itens.size() ; i++) {
			for(int j = 0 ; j < this.itens.size() ;j++) {
				mat[i][j] = Kmer.Aresta.UNDEFINED;
			}
		}
	}
	
	private void montarMatriz() {
		for(int i = 0 ; i < itens.size() ; i++) {
			for(int j = 0 ; j < itens.size() ;j++) {
				if(i == j && mat[i][j] == Kmer.Aresta.UNDEFINED) continue;
				
				if(itens.get(j).sufixoEIgualA(itens.get(i).getPrefixo())) {
					mat[i][j] = Kmer.Aresta.DIREITA;
					mat[j][i] = Kmer.Aresta.ESQUERDA;
					itens.get(i).informarQueONoFoiConectadoAOutro();
				}else if(itens.get(j).prefixoEIgualA(itens.get(i).getSufixo())) {
					mat[i][j] = Kmer.Aresta.ESQUERDA;
					mat[j][i] = Kmer.Aresta.DIREITA;
					itens.get(i).informarQueONoFoiConectadoAOutro();
				}
			}
		}
	}
	
	private Integer descubridor() {
		//verifica todos os nós que tem uma quantidade impar de ligações (se mais de dois o algoritmo esta errado)
		//  - dentre esses, encontrar o no que tem uma quantidade impar de ligações KMER.ARESTA.ESQUERDA. ele será o começo.

		List<Integer> candidatos = new ArrayList<Integer>();
		
		int contador = 0;
		for(Kmer k : itens) {
//			System.out.println("Quantidade " + k.getQuantidadeDeNosConectados() % 2);
			if(k.getQuantidadeDeNosConectados() % 2 == 1) {
				candidatos.add(contador);
			}
			contador++;
		}
		
		if(candidatos.size() > 2) {
			System.err.println("Algoritmo errado.");
			return -1;
		}
		try {
//			System.out.println("O começo esta em " + itens.get(candidatos.get(0)).verConteudo());
			int qtd_esq = 0;
			for(int j = 0 ; j < this.itens.size() ; j++) {
				if(mat[candidatos.get(0)][j] == Kmer.Aresta.ESQUERDA) qtd_esq++;
			}
			if(qtd_esq % 2 == 1) {
//				System.out.println("Okay, começa pela linha " + candidatos.get(0));
				return candidatos.get(0);
			}else {
				throw new IOException("Não é o primeiro");
			}
		}catch(Exception _) {
			int qtd_esq = 0;
			for(int j = 0 ; j < this.itens.size() ; j++) {
				if(mat[candidatos.get(1)][j] == Kmer.Aresta.ESQUERDA) qtd_esq++;
			}
			if(qtd_esq % 2 == 1) {
//				System.out.println("Okay, começa pela linha " + candidatos.get(1));
				return candidatos.get(1);
			}else {
				System.err.println("Algoritmo errando na verificação dos candidatos");
				return -1;
			}
		}
	}
	
	/**
	 * 
	 * @param linha
	 * @return a coluna onde se encontra uma ligação
	 */
	private int buscador_pivo_coluna(int linha) {
		for(int j = 0 ; j < itens.size() ; j++) {
			if(mat[linha][j] != Kmer.Aresta.UNDEFINED) {
				return j;
			}
		}
		
		return -1;
	}
	
	/**
	 * 
	 * @param coluna
	 * @return a linha onde se encontra a ligação
	 */
	private int buscador_pivo_liinha(int coluna) {
		for(int i = 0 ; i < itens.size() ; i++) {
			if(mat[i][coluna] != Kmer.Aresta.UNDEFINED) {
				return i;
			}
		}
		
		return -1;
	}
	
	/**
	 * Mapear as letras a partir do inicio encontrado anteriormente
	 * @param linha_inicial
	 */
	private void rastreador(int linha_inicial) {
		int PIVO_LINHA = linha_inicial, PIVO_COLUNA = -1;
		int ultimaLetra = itens.get(0).verConteudo().length() - 1;
		//primeira interação
		for(int j = 0 ; j < itens.size() ; j++) {
			if(mat[PIVO_LINHA][j] == Kmer.Aresta.ESQUERDA) {
				 PIVO_COLUNA = j;
				 break;
			}
		}
		
		resposta += itens.get(PIVO_LINHA).verConteudo();
		mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
		int aux = PIVO_LINHA;
		PIVO_LINHA = PIVO_COLUNA;
		PIVO_COLUNA = aux;
		mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
		qtd_ja_montado++;
//		System.out.println(resposta);
		
		//ciclo importante
		while(true) {
			int buscar_coluna = buscador_pivo_coluna(PIVO_LINHA);
			int buscar_linha = buscador_pivo_liinha(PIVO_COLUNA);
			
			if(buscar_coluna == -1 && buscar_linha == -1) {
				System.out.println("Solução encontrada e algoritmo terminado com exito");
				break;
			}
			
			qtd_ja_montado++;
			if(buscar_coluna != -1) {
				PIVO_COLUNA = buscar_coluna;
				
				if(mat[PIVO_LINHA][PIVO_COLUNA] == Kmer.Aresta.ESQUERDA) {
					resposta += itens.get(PIVO_LINHA).verConteudo().charAt(ultimaLetra);
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
					aux = PIVO_LINHA;
					PIVO_LINHA = PIVO_COLUNA;
					PIVO_COLUNA = aux;
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
				}else {//se fosse direita
					resposta += itens.get(PIVO_COLUNA).verConteudo().charAt(ultimaLetra);
					mat[PIVO_COLUNA][PIVO_LINHA] = Kmer.Aresta.UNDEFINED;//vou para o da esquerda e anoto e dps apago o registro
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;//volto pra direita e apago o registro pra busca outro pivo
				}
				if(qtd_ja_montado == itens.size() - 1) {
					resposta += itens.get(PIVO_LINHA).verConteudo().charAt(ultimaLetra);
				}
				continue;
			}
			if(buscar_linha != -1) {
				PIVO_LINHA = buscar_linha;
				
				if(mat[PIVO_LINHA][PIVO_COLUNA] == Kmer.Aresta.ESQUERDA) {
					resposta += itens.get(PIVO_LINHA).verConteudo().charAt(ultimaLetra);
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
					aux = PIVO_LINHA;
					PIVO_LINHA = PIVO_COLUNA;
					PIVO_COLUNA = aux;
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;
				}else {//se fosse direita
					resposta += itens.get(PIVO_COLUNA).verConteudo().charAt(ultimaLetra);
					mat[PIVO_COLUNA][PIVO_LINHA] = Kmer.Aresta.UNDEFINED;//vou para o da esquerda e anoto e dps apago o registro
					mat[PIVO_LINHA][PIVO_COLUNA] = Kmer.Aresta.UNDEFINED;//volto pra direita e apago o registro pra busca outro pivo
				}
				if(qtd_ja_montado == itens.size() - 1) {
					resposta += itens.get(PIVO_LINHA).verConteudo().charAt(ultimaLetra);
				}
				continue;
			}
		}
		
		System.out.println("Resposta: " + resposta);
	}
	
	public String getResposta() {
		return resposta;
	}
	
	@Override
	public String toString() {
		String str = "Itens: {";
		for(Kmer k : this.itens) {
			str += k.verConteudo() + ", ";
		}
		str += "}\n\n";
		
		for(int i = 0 ; i < itens.size() ; i++) {
			for(int j = 0 ; j < itens.size() ; j++) {
				str += mat[i][j].name() + "    ";
			}
			str += "\n";
		}
		return str;
	}
	
	public static void main(String[] args) throws IOException {
//		String [] CASA = {"ASA", "CAS"};
//		String [] MACARRAO = {"ACA","ARR", "CAR", "MAC", "RAO", "RRA"};
//		String [] PARALELEPIPEDO = {"ALE", "ARA", "EDO", "ELE", "EPI", "IPE", "LEL", "LEP", "PAR", "PED", "PIP"};
//		String [] data = ddata.split(",");
//		for(String d : data) {
//			System.out.println(d);
//			
//		}
		System.out.print("Informe o nome do arquivo: ");
		String [] dados = Arquivo.apenasLer(sc.nextLine()).split(",");
		System.out.print("A seguir, informe o nome do arquivo de saída na qual a resposta serágravada (com sua extensão inclusa): ");
		String nome_arquivo_saida = sc.nextLine();
		System.out.println("Iniciando programa.");
		Arquivo.escreverEmArquivoEspecifico(nome_arquivo_saida, new Assembler(dados, dados.length).getResposta());
		sc.close();
		System.out.println("A resposta foi gravada no arquivo " + nome_arquivo_saida + ".");
	}
}