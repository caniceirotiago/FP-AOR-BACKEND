package aor.fpbackend.utils;

import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import jakarta.ejb.Stateless;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

@Stateless
public class PdfGenerator {

    public void generateProjectReport(ReportSummaryDto reportSummary, String dest) throws IOException {
        // Load the logo image
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("CriticalLogo.png")) {
            if (logoStream == null) {
                throw new FileNotFoundException("Logo file not found");
            }
            // Create a new document
            PDDocument document = new PDDocument();
            try {
                // Initialize page and content stream
                PDPage page = new PDPage();
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);

                // Define starting position and line height
                float margin = 50; // Margin from top of the page
                float yPosition = page.getMediaBox().getHeight() - margin; // Start at the top margin
                float lineHeight = 12; // Adjust line height as needed

                // Draw the logo at the top of the page
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(logoStream), "logo");
                contentStream.drawImage(logo, 450, yPosition - 10, 100, 50); // Adjust x, y, width, height as needed

                // Title
                yPosition -= 10; // Adjust for the logo height
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 16, "Project Report Summary", margin + 140, yPosition);
                yPosition -= lineHeight * 4; // Move down for the next line

                // Average Members per Project
                drawText(contentStream, PDType1Font.HELVETICA, 12, "Average Members per Project: " + reportSummary.getAverageMembersPerProject().getAverage(), margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Average Project Duration
                drawText(contentStream, PDType1Font.HELVETICA, 12, "Average Project Duration: " + reportSummary.getAverageProjectDuration().getAverage(), margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Project Count by Location
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Project Count by Location:", margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Iterate over Project Count by Location
                yPosition = drawLocationData(contentStream, reportSummary.getProjectCountByLocation(), margin, yPosition, lineHeight);

                // Approved Projects by Location
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Approved Projects by Location:", margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Iterate over Approved Projects by Location
                yPosition = drawLocationData(contentStream, reportSummary.getApprovedProjectsByLocation(), margin, yPosition, lineHeight);

                // Completed Projects by Location
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Completed Projects by Location:", margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Iterate over Completed Projects by Location
                yPosition = drawLocationData(contentStream, reportSummary.getCompletedProjectsByLocation(), margin, yPosition, lineHeight);

                // Canceled Projects by Location
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Canceled Projects by Location:", margin, yPosition);
                yPosition -= lineHeight * 2; // Move down for the next section

                // Iterate over Canceled Projects by Location
                yPosition = drawLocationData(contentStream, reportSummary.getCanceledProjectsByLocation(), margin, yPosition, lineHeight);

                // Close the content stream
                contentStream.close();

                // Save the document
                document.save(dest);
            } finally {
                // Close the document
                document.close();
            }
        }
    }

    // Helper method to draw text on the PDF
    private void drawText(PDPageContentStream contentStream, PDType1Font font, int fontSize, String text, float x, float y) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    // Helper method to draw location data on the PDF
    private float drawLocationData(PDPageContentStream contentStream, Iterable<ReportProjectsLocationDto> locationData, float x, float y, float lineHeight) throws IOException {
        for (ReportProjectsLocationDto locationDto : locationData) {
            String locationText = locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%";
            drawText(contentStream, PDType1Font.HELVETICA, 12, locationText, x + 20, y);
            y -= lineHeight * 2; // Move down for the next location
        }
        return y;
    }

    // Method for Assets!!!
    public void generateAssetReport(ReportSummaryDto reportSummary, String dest) throws IOException {

    }
}