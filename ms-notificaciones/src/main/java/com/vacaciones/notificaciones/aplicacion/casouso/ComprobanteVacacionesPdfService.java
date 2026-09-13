package com.vacaciones.notificaciones.aplicacion.casouso;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Genera el PDF "Comprobante de vacaciones" que se adjunta al email de aprobacion.
 * Es logica pura (sin I/O externo): no necesita un puerto/adaptador, solo se invoca
 * directamente desde el consumer de 'solicitud.aprobada'.
 */
@ApplicationScoped
public class ComprobanteVacacionesPdfService {

    private static final DateTimeFormatter FECHA_CORTA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FECHA_EMISION =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));

    public byte[] generar(
            String nombreColaborador,
            LocalDate fechaInicio,
            LocalDate fechaFin,
            BigDecimal dias,
            String nombreAprobador) {
        try {
            ByteArrayOutputStream salida = new ByteArrayOutputStream();
            Document documento = new Document(PageSize.A4, 56, 56, 56, 56);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
            Font textoFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
            Font negritaFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph titulo = new Paragraph("COMPROBANTE DE VACACIONES", tituloFont);
            titulo.setAlignment(Element.ALIGN_CENTER);
            titulo.setSpacingAfter(24);
            documento.add(titulo);

            LocalDate fechaRetorno = fechaFin.plusDays(1);
            String diasTexto = dias.stripTrailingZeros().toPlainString();

            Paragraph cuerpo = new Paragraph();
            cuerpo.setFont(textoFont);
            cuerpo.setLeading(16);
            cuerpo.add("En cumplimiento de las disposiciones legales vigentes, se deja constancia que el/la Sr(a): ");
            cuerpo.add(new com.lowagie.text.Chunk(nombreColaborador, negritaFont));
            cuerpo.add(", hará uso de ");
            cuerpo.add(new com.lowagie.text.Chunk(diasTexto + " días", negritaFont));
            cuerpo.add(" de vacaciones, en las siguientes fechas:");
            cuerpo.setSpacingAfter(18);
            documento.add(cuerpo);

            PdfPTable tabla = new PdfPTable(2);
            tabla.setWidthPercentage(60);
            tabla.setHorizontalAlignment(Element.ALIGN_LEFT);
            tabla.setWidths(new float[]{1.3f, 1f});
            agregarFila(tabla, "Fecha Inicio", fechaInicio.format(FECHA_CORTA), labelFont, negritaFont);
            agregarFila(tabla, "Fecha Fin", fechaFin.format(FECHA_CORTA), labelFont, negritaFont);
            agregarFila(tabla, "Fecha Retorno", fechaRetorno.format(FECHA_CORTA), labelFont, negritaFont);
            tabla.setSpacingAfter(24);
            documento.add(tabla);

            Paragraph constancia = new Paragraph(
                    "Quedando constancia para los fines que se estimen convenientes.", textoFont);
            constancia.setSpacingAfter(18);
            documento.add(constancia);

            Paragraph fecha = new Paragraph(capitalizar(LocalDate.now().format(FECHA_EMISION)), textoFont);
            fecha.setSpacingAfter(48);
            documento.add(fecha);

            PdfPTable firmas = new PdfPTable(2);
            firmas.setWidthPercentage(100);
            firmas.setSpacingBefore(32);
            firmas.addCell(celdaFirma(
                    "Aprobado por:", nombreAprobador != null ? nombreAprobador : "Aprobado electrónicamente",
                    labelFont, negritaFont));
            firmas.addCell(celdaFirma("Trabajador:", nombreColaborador, labelFont, negritaFont));
            documento.add(firmas);

            documento.close();
            return salida.toByteArray();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el comprobante de vacaciones en PDF", e);
        }
    }

    private void agregarFila(PdfPTable tabla, String etiqueta, String valor, Font labelFont, Font valorFont) {
        PdfPCell celdaEtiqueta = new PdfPCell(new com.lowagie.text.Phrase(etiqueta, labelFont));
        celdaEtiqueta.setBorder(0);
        celdaEtiqueta.setPaddingBottom(6);
        tabla.addCell(celdaEtiqueta);

        PdfPCell celdaValor = new PdfPCell(new com.lowagie.text.Phrase(valor, valorFont));
        celdaValor.setBorder(0);
        celdaValor.setPaddingBottom(6);
        tabla.addCell(celdaValor);
    }

    private PdfPCell celdaFirma(String etiqueta, String nombre, Font labelFont, Font nombreFont) {
        Paragraph contenido = new Paragraph();
        contenido.add(new com.lowagie.text.Chunk("_________________________\n", labelFont));
        contenido.add(new com.lowagie.text.Chunk(etiqueta + " ", labelFont));
        contenido.add(new com.lowagie.text.Chunk(nombre, nombreFont));
        PdfPCell celda = new PdfPCell(contenido);
        celda.setBorder(0);
        celda.setPaddingTop(24);
        return celda;
    }

    private String capitalizar(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
