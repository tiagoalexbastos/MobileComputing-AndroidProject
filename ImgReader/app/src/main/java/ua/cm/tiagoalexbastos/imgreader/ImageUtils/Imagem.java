package ua.cm.tiagoalexbastos.imgreader.ImageUtils;

/**
 * Created by tiagoalexbastos on 26-03-2017.
 */

public class Imagem {
    String resultados;
    String imagem;

    public Imagem() {
    }

    public Imagem(String imagem, String resultados) {
        this.imagem = imagem;
        this.resultados = resultados;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public String getResultados() {

        return resultados;
    }

    public void setResultados(String resultados) {
        this.resultados = resultados;
    }
}
