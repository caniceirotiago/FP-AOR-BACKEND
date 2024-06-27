package aor.fpbackend.utils;

import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import jakarta.ejb.Stateless;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

@Stateless
public class PdfReportGenerator implements Closeable {

    private static final int MARGIN = 50;
    private static final int FONT_SIZE_TITLE = 16;
    private static final int FONT_SIZE_NORMAL = 12;

    private PDDocument document;
    private PDPageContentStream contentStream;
    private float yPosition;

    public PdfReportGenerator() {
        this.document = new PDDocument();
        this.yPosition = 0;
    }

    public void generateProjectReport(ReportSummaryDto reportSummary, String dest) throws IOException {
        try {
            // Create a new page
            PDPage page = new PDPage();
            document.addPage(page);
            // Start a new content stream
           contentStream = new PDPageContentStream(document, page);
                yPosition = page.getMediaBox().getHeight() - MARGIN;
            // Add content to the report
            yPosition = addTitle("Project Report Summary");
            yPosition = addText("Average Members per Project: " + reportSummary.getAverageMembersPerProject().getAverage());
            yPosition = addText("Average Project Duration: " + reportSummary.getAverageProjectDuration().getAverage());
            yPosition = addProjectCountByLocation("Project Count by Location:", reportSummary.getProjectCountByLocation());
            yPosition = addProjectsByLocation("Approved Projects by Location:", reportSummary.getApprovedProjectsByLocation());
            yPosition = addProjectsByLocation("Completed Projects by Location:", reportSummary.getCompletedProjectsByLocation());
            yPosition = addProjectsByLocation("Canceled Projects by Location:", reportSummary.getCanceledProjectsByLocation());

        } finally {
            close();
            document.save(dest);
        }
    }

    private float addTitle(String text) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, FONT_SIZE_TITLE);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - MARGIN;
    }

    private float addText(String text) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(text);
        contentStream.endText();
        return yPosition - MARGIN;
    }

    private float addProjectCountByLocation(String title, List<ReportProjectsLocationDto> projectCountByLocation) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= MARGIN;

        for (ReportProjectsLocationDto locationDto : projectCountByLocation) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN * 2, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= MARGIN;
            if (yPosition < MARGIN) {
                contentStream.close();
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;
            }
        }

        return yPosition;
    }

    private float addProjectsByLocation(String title, List<ReportProjectsLocationDto> projects) throws IOException {
        contentStream.setFont(PDType1Font.HELVETICA, FONT_SIZE_NORMAL);
        contentStream.beginText();
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText(title);
        contentStream.endText();
        yPosition -= MARGIN;

        for (ReportProjectsLocationDto locationDto : projects) {
            contentStream.beginText();
            contentStream.newLineAtOffset(MARGIN * 2, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= MARGIN;
            if (yPosition < MARGIN) {
                contentStream.close();
                PDPage newPage = new PDPage();
                document.addPage(newPage);
                contentStream = new PDPageContentStream(document, newPage);
                yPosition = newPage.getMediaBox().getHeight() - MARGIN;
            }
        }

        return yPosition;
    }

    @Override
    public void close() throws IOException {
        if (contentStream != null) {
            contentStream.close();
        }
        if (document != null) {
            document.close();
        }
    }
}
