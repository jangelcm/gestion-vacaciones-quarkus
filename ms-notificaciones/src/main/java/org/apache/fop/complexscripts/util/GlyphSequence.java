package org.apache.fop.complexscripts.util;

import java.nio.IntBuffer;
import java.util.List;

/**
 * Stub minimo de org.apache.fop.complexscripts.util.GlyphSequence, NO la implementacion real
 * de Apache FOP. Existe solo para que GraalVM native-image pueda enlazar
 * com.lowagie.text.pdf.FopGlyphProcessor (OpenPDF), que referencia esta clase para el soporte
 * opcional de scripts complejos (arabe/tailandes/etc.) que este proyecto no usa (solo texto en
 * espanol via fuentes estandar). Al no declarar Apache FOP como dependencia real, sin este stub
 * native-image falla con "unresolved type" al intentar enlazar TODO el classpath en build time.
 * Si algun dia se necesita generar PDFs con scripts complejos, hay que quitar este stub y agregar
 * la dependencia real org.apache.xmlgraphics:fop.
 */
public class GlyphSequence {

    public GlyphSequence(IntBuffer characters, IntBuffer glyphs, List<?> associations) {
        throw new UnsupportedOperationException(
                "org.apache.fop.complexscripts.util.GlyphSequence es un stub; "
                        + "este proyecto no soporta scripts complejos en el comprobante PDF");
    }

    public IntBuffer getGlyphs() {
        throw new UnsupportedOperationException(
                "org.apache.fop.complexscripts.util.GlyphSequence es un stub; "
                        + "este proyecto no soporta scripts complejos en el comprobante PDF");
    }
}
