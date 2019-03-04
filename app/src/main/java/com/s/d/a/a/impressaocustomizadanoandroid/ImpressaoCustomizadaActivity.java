package com.s.d.a.a.impressaocustomizadanoandroid;

import java.io.FileOutputStream;
import java.io.IOException;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.content.Context;
import android.print.PrintDocumentInfo;
import android.print.pdf.PrintedPdfDocument;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.print.PrintManager;
import android.view.View;

public class ImpressaoCustomizadaActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_impressao_customizada);
    }

    public class CustomPrintDocumentAdapter extends PrintDocumentAdapter
    {
        Context contexto;
        private int alturaPagina;
        private int larguraPagina;
        public PdfDocument documentoPDF;
        public int totalDePaginas = 4;

        public CustomPrintDocumentAdapter(Context context)
        {
            this.contexto = context;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes,
                             PrintAttributes newAttributes,
                             CancellationSignal cancellationSignal,
                             LayoutResultCallback callback,
                             Bundle metadata) {
            documentoPDF = new PrintedPdfDocument(contexto, newAttributes);

            alturaPagina = newAttributes.getMediaSize().getHeightMils()/1000 * 72;
            larguraPagina    = newAttributes.getMediaSize().getWidthMils()/1000 * 72;

            if (cancellationSignal.isCanceled() ) {
                callback.onLayoutCancelled();
                return;
            }

            if (totalDePaginas > 0) {
                PrintDocumentInfo.Builder builder = new PrintDocumentInfo
                        .Builder("impressao_saida.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(totalDePaginas);

                PrintDocumentInfo info = builder.build();
                callback.onLayoutFinished(info, true);
            } else {
                callback.onLayoutFailed("Contador de páginas = ZERO.");
            }

        }

        @Override
        public void onWrite(final PageRange[] pageRanges,
                            final ParcelFileDescriptor destination,
                            final CancellationSignal
                                    cancellationSignal,
                            final WriteResultCallback callback) {
            for (int i = 0; i < totalDePaginas; i++) {
                if (pageInRange(pageRanges, i))
                {
                    PageInfo newPage = new PageInfo.Builder(larguraPagina,
                            alturaPagina, i).create();

                    PdfDocument.Page page =
                            documentoPDF.startPage(newPage);

                    if (cancellationSignal.isCanceled()) {
                        callback.onWriteCancelled();
                        documentoPDF.close();
                        documentoPDF = null;
                        return;
                    }
                    drawPage(page, i);
                    documentoPDF.finishPage(page);
                }
            }

            try {
                documentoPDF.writeTo(new FileOutputStream(
                        destination.getFileDescriptor()));
            } catch (IOException e) {
                callback.onWriteFailed(e.toString());
                return;
            } finally {
                documentoPDF.close();
                documentoPDF = null;
            }

            callback.onWriteFinished(pageRanges);

        }

        private boolean pageInRange(PageRange[] pageRanges, int page)
        {
            for (int i = 0; i<pageRanges.length; i++)
            {
                if ((page >= pageRanges[i].getStart()) &&
                        (page <= pageRanges[i].getEnd()))
                    return true;
            }
            return false;
        }

        private void drawPage(PdfDocument.Page page,
                              int pagenumber) {
            Canvas canvas = page.getCanvas();

            pagenumber++; // tenha certeza que o número da página começa em 1

            int titleBaseLine = 72;
            int leftMargin = 54;

            Paint paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setTextSize(40);
            canvas.drawText(
                    "Imprimir página do documento de teste " + pagenumber,
                    leftMargin,
                    titleBaseLine,
                    paint);

            paint.setTextSize(14);
            canvas.drawText("Teste de impressão de documentos", leftMargin, titleBaseLine + 35, paint);

            if (pagenumber % 2 == 0)
                paint.setColor(Color.RED);
            else
                paint.setColor(Color.GREEN);

            PageInfo pageInfo = page.getInfo();


            canvas.drawCircle(pageInfo.getPageWidth()/2,
                    pageInfo.getPageHeight()/2,
                    150,
                    paint);
        }

    }

    public void imprimirDocumento(View view)
    {
        PrintManager printManager = (PrintManager) this
                .getSystemService(Context.PRINT_SERVICE);

        String jobName = this.getString(R.string.app_name) +
                " Documento";

        printManager.print(jobName, new CustomPrintDocumentAdapter(this), null);
    }
}
