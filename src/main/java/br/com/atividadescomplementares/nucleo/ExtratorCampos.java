package br.com.atividadescomplementares.nucleo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExtratorCampos {

    private static final Map<String, Integer> MESES = new HashMap<>();

    static {
        MESES.put("janeiro", 1);
        MESES.put("fevereiro", 2);
        MESES.put("março", 3);
        MESES.put("marco", 3);
        MESES.put("abril", 4);
        MESES.put("maio", 5);
        MESES.put("junho", 6);
        MESES.put("julho", 7);
        MESES.put("agosto", 8);
        MESES.put("setembro", 9);
        MESES.put("outubro", 10);
        MESES.put("novembro", 11);
        MESES.put("dezembro", 12);
    }

    private static final Pattern[] PADROES_HORAS = {
            Pattern.compile("carga\\s+hor[aá]ria\\s+de\\s+(\\d+[.,]?\\d*)\\s*horas?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("carga\\s+hor[aá]ria\\s*[:\\-]?\\s*(\\d+[.,]?\\d*)\\s*h(?:oras?)?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("\\bCH\\s*[:\\-]?\\s*(\\d+[.,]?\\d*)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("dura[cç][aã]o\\s*[:\\-]?\\s*de?\\s*(\\d+[.,]?\\d*)\\s*horas?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+[.,]?\\d*)\\s*horas?\\b", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(\\d+[.,]?\\d*)\\s*h\\b", Pattern.CASE_INSENSITIVE)
    };

    private static final Pattern PADRAO_DATA_NUMERICA =
            Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");

    private static final Pattern PADRAO_DATA_POR_EXTENSO = Pattern.compile(
            "(\\d{1,2})\\s+de\\s+(janeiro|fevereiro|mar[cç]o|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)\\s+de\\s+(\\d{4})",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern PADRAO_CODIGO_VERIFICACAO = Pattern.compile(
            "c[oó]digo\\s+de\\s+verifica[cç][aã]o\\s*[:\\-]?\\s*([A-Za-z0-9\\-]+)", Pattern.CASE_INSENSITIVE);

    private static final Pattern PADRAO_PARTICIPANTE = Pattern.compile(
            "certificamos\\s+que\\s+([A-ZÀ-Ý][\\p{L} .'\\-]+?)\\s+(?:participou|concluiu|cursou|frequentou|realizou|atuou)",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern[] PADROES_TITULO = {
            Pattern.compile("[\"“]([^\"”]+)[\"”]"),
            Pattern.compile(
                    "(?:curso|palestra|minicurso|oficina|workshop|programa\\s+de\\s+extens[aã]o|projeto\\s+de\\s+extens[aã]o|est[aá]gio|monitoria)\\s+(?:de|em|no\\s+curso\\s+de)?\\s*([^,;.\"”]+)",
                    Pattern.CASE_INSENSITIVE)
    };

    private static final Pattern[] PADROES_EMISSOR = {
            Pattern.compile("(?:promovido|realizado|oferecido|ministrado|organizado)\\s+pel[oa]\\s+([^,;.]+)",
                    Pattern.CASE_INSENSITIVE),
            Pattern.compile("(?:na\\s+empresa|na\\s+institui[cç][aã]o)\\s+([^,;.]+)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("emitido\\s+por\\s*[:\\-]?\\s*([^,;.]+)", Pattern.CASE_INSENSITIVE)
    };

    public CamposExtraidos extrair(String textoOriginal) {
        String texto = textoOriginal == null ? "" : textoOriginal.replaceAll("\\s+", " ").trim();

        String participante = buscarGrupo(PADRAO_PARTICIPANTE, texto, 1);
        String titulo = buscarPrimeiraCorrespondencia(PADROES_TITULO, texto);
        String emissor = buscarPrimeiraCorrespondencia(PADROES_EMISSOR, texto);
        LocalDate data = extrairData(texto);
        Double horas = extrairHoras(texto);
        String codigoVerificacao = buscarGrupo(PADRAO_CODIGO_VERIFICACAO, texto, 1);

        List<String> camposFaltando = new ArrayList<>();
        if (titulo == null || titulo.isBlank()) camposFaltando.add("titulo");
        if (emissor == null || emissor.isBlank()) camposFaltando.add("emissor");
        if (data == null) camposFaltando.add("data");
        if (horas == null) camposFaltando.add("horas");
        if (participante == null || participante.isBlank()) camposFaltando.add("participante");

        return new CamposExtraidos(
                limpar(titulo),
                limpar(emissor),
                data,
                horas,
                codigoVerificacao == null ? null : codigoVerificacao.trim(),
                limpar(participante),
                camposFaltando
        );
    }

    private Double extrairHoras(String texto) {
        for (Pattern padrao : PADROES_HORAS) {
            Matcher correspondencia = padrao.matcher(texto);
            if (correspondencia.find()) {
                String numero = correspondencia.group(1).replace(",", ".");
                try {
                    return Double.parseDouble(numero);
                } catch (NumberFormatException erro) {
                    return null;
                }
            }
        }
        return null;
    }

    private LocalDate extrairData(String texto) {
        Matcher numerica = PADRAO_DATA_NUMERICA.matcher(texto);
        if (numerica.find()) {
            try {
                int dia = Integer.parseInt(numerica.group(1));
                int mes = Integer.parseInt(numerica.group(2));
                int ano = Integer.parseInt(numerica.group(3));
                return LocalDate.of(ano, mes, dia);
            } catch (Exception erro) {
                return null;
            }
        }

        Matcher porExtenso = PADRAO_DATA_POR_EXTENSO.matcher(texto);
        if (porExtenso.find()) {
            try {
                int dia = Integer.parseInt(porExtenso.group(1));
                String nomeMes = normalizarMes(porExtenso.group(2));
                Integer mes = MESES.get(nomeMes);
                int ano = Integer.parseInt(porExtenso.group(3));
                if (mes != null) {
                    return LocalDate.of(ano, mes, dia);
                }
            } catch (Exception erro) {
                return null;
            }
        }

        return null;
    }

    private String normalizarMes(String mes) {
        return mes.toLowerCase()
                .replace("á", "a")
                .replace("ç", "c");
    }

    private String buscarGrupo(Pattern padrao, String texto, int grupo) {
        Matcher correspondencia = padrao.matcher(texto);
        if (correspondencia.find()) {
            return correspondencia.group(grupo);
        }
        return null;
    }

    private String buscarPrimeiraCorrespondencia(Pattern[] padroes, String texto) {
        for (Pattern padrao : padroes) {
            String resultado = buscarGrupo(padrao, texto, 1);
            if (resultado != null && !resultado.isBlank()) {
                return resultado;
            }
        }
        return null;
    }

    private String limpar(String texto) {
        if (texto == null) {
            return null;
        }
        String resultado = texto.trim();
        while (resultado.endsWith(".") || resultado.endsWith(",")) {
            resultado = resultado.substring(0, resultado.length() - 1).trim();
        }
        return resultado;
    }
}
