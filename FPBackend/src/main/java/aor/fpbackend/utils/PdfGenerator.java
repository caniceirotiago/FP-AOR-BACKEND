package aor.fpbackend.utils;

import aor.fpbackend.dto.Report.ReportAssetSummaryDto;
import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportProjectSummaryDto;
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

    // Method for Project Report!
    public void generateProjectReport(ReportProjectSummaryDto reportSummary, String dest) throws IOException {
        // Load the logo image
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("CriticalLogo.png")) {
            if (logoStream == null) {
                throw new FileNotFoundException("Logo file not found");
            }
            // Create a new document
            PDDocument document = new PDDocument();
            try {
                // Initialize pages and content streams
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(logoStream), "logo");
                PDPageContentStream contentStream = null;

                // First Page
                PDPage firstPage = new PDPage();
                document.addPage(firstPage);
                contentStream = new PDPageContentStream(document, firstPage);
                insertLogo(contentStream, logo, firstPage.getMediaBox().getWidth());

                // Define starting position and line height
                float xMargin = 120;
                float yMargin = 80;
                float[] yPosition = new float[]{firstPage.getMediaBox().getHeight() - yMargin}; // Start at the top margin
                float lineHeight = 12;

                // Title
                yPosition[0] -= 40;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 16, "Project Report", xMargin + 100, yPosition[0]);
                yPosition[0] -= lineHeight * 4;

                // Average Members per Project - title and value
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Average Members per Project: ", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.2;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA, 12, reportSummary.getAverageMembersPerProject().getAverage() + " members", xMargin + 20, yPosition[0]);
                yPosition[0] -= lineHeight * 2;

                // Average Project Duration - title and value
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Average Project Duration: ", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.2;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA, 12, reportSummary.getAverageProjectDuration().getAverage() + " days", xMargin + 20, yPosition[0]);
                yPosition[0] -= lineHeight * 2;
                // Project Count by Location
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Project Count by Location:", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 2;
                yPosition[0] = drawLocationData(contentStream, reportSummary.getProjectCountByLocation(), xMargin, yPosition[0], lineHeight);

                // Footer for first page
                drawText(contentStream, PDType1Font.HELVETICA, 10, "Page 1 of 2", 500, 30);
                // Close the content stream for the first page
                contentStream.close();

                // Add second page and its content
                PDPage secondPage = new PDPage();
                document.addPage(secondPage);
                contentStream = new PDPageContentStream(document, secondPage);
                insertLogo(contentStream, logo, secondPage.getMediaBox().getWidth());

                // Approved Projects by Location
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Approved Projects by Location:", xMargin, secondPage.getMediaBox().getHeight() - yMargin - 50);
                yPosition[0] -= lineHeight * 1.2;
                yPosition[0] = drawLocationData(contentStream, reportSummary.getApprovedProjectsByLocation(), xMargin, yPosition[0], lineHeight);

                // Completed Projects by Location
                yPosition[0] -= lineHeight * 1;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Completed Projects by Location:", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.2;
                yPosition[0] = drawLocationData(contentStream, reportSummary.getCompletedProjectsByLocation(), xMargin, yPosition[0], lineHeight);

                // Canceled Projects by Location
                yPosition[0] -= lineHeight * 1;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Canceled Projects by Location:", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.2;
                yPosition[0] = drawLocationData(contentStream, reportSummary.getCanceledProjectsByLocation(), xMargin, yPosition[0], lineHeight);

                // Footer for second page
                drawText(contentStream, PDType1Font.HELVETICA, 10, "Page 2 of 2", 500, 30);
                // Close the content stream for the second page
                contentStream.close();

                // Save the document
                document.save(dest);
            } finally {
                // Close the document
                document.close();
            }
        }
    }

    // Method for Asset Report!
    public void generateAssetReport(ReportAssetSummaryDto assetReportSummary, String dest) throws IOException {
        // Load the logo image
        try (InputStream logoStream = getClass().getClassLoader().getResourceAsStream("CriticalLogo.png")) {
            if (logoStream == null) {
                throw new FileNotFoundException("Logo file not found");
            }
            // Create a new document
            PDDocument document = new PDDocument();
            try {
                // Initialize pages and content streams
                PDImageXObject logo = PDImageXObject.createFromByteArray(document, IOUtils.toByteArray(logoStream), "logo");
                PDPageContentStream contentStream = null;

                // Create Page
                PDPage firstPage = new PDPage();
                document.addPage(firstPage);
                contentStream = new PDPageContentStream(document, firstPage);
                insertLogo(contentStream, logo, firstPage.getMediaBox().getWidth());

                // Define starting position and line height
                float xMargin = 120;
                float yMargin = 80;
                float[] yPosition = new float[]{firstPage.getMediaBox().getHeight() - yMargin}; // Start at the top margin
                float lineHeight = 12;

                // Title
                yPosition[0] -= 40;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 16, "Asset Report", xMargin + 100, yPosition[0]);
                yPosition[0] -= lineHeight * 4;

                // Used Quantity by Project - title and value
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Top 5 Used Quantity by Project: ", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.5;
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Project Name:", xMargin + 20, yPosition[0]);
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Resource Qty:", xMargin + 150, yPosition[0]);
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Component Qty:", xMargin + 280, yPosition[0]);
                yPosition[0] -= lineHeight * 1.5;

                // Iterate over used quantity by project data
                for (Object[] result : assetReportSummary.getTopProjectsByUsedQuantity()) {
                    String projectName = (String) result[0];
                    Long resourceQuantity = (Long) result[1];
                    Long componentQuantity = (Long) result[2];
                    drawText(contentStream, PDType1Font.HELVETICA, 12, projectName, xMargin + 20, yPosition[0]);
                    drawText(contentStream, PDType1Font.HELVETICA, 12, String.valueOf(resourceQuantity), xMargin + 170, yPosition[0]);
                    drawText(contentStream, PDType1Font.HELVETICA, 12, String.valueOf(componentQuantity), xMargin + 300, yPosition[0]);
                    yPosition[0] -= lineHeight * 1.5;
                }

                // Title for top assets
                yPosition[0] -= lineHeight * 3;
                yPosition[0] = drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Top 5 Used Quantity by Asset: ", xMargin, yPosition[0]);
                yPosition[0] -= lineHeight * 1.5;
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Asset Name:", xMargin + 20, yPosition[0]);
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Type:", xMargin + 150, yPosition[0]);
                drawText(contentStream, PDType1Font.HELVETICA_BOLD, 12, "Used Quantity:", xMargin + 280, yPosition[0]);
                yPosition[0] -= lineHeight * 1.5;

                // Iterate over top assets by used quantity data
                for (Object[] result : assetReportSummary.getTopAssetsByUsedQuantity()) {
                    String assetName = (String) result[0];
                    String assetType = result[1].toString();
                    Long totalUsedQuantity = (Long) result[2];
                    drawText(contentStream, PDType1Font.HELVETICA, 12, assetName, xMargin + 20, yPosition[0]);
                    drawText(contentStream, PDType1Font.HELVETICA, 12, assetType, xMargin + 150, yPosition[0]);
                    drawText(contentStream, PDType1Font.HELVETICA, 12, String.valueOf(totalUsedQuantity), xMargin + 300, yPosition[0]);
                    yPosition[0] -= lineHeight * 1.5;
                }

                // Footer
                drawText(contentStream, PDType1Font.HELVETICA, 10, "Page 1 of 1", 500, 30);
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

    // Helper method to insert logo
    private void insertLogo(PDPageContentStream contentStream, PDImageXObject logo, float pageWidth) throws IOException {
        contentStream.drawImage(logo, pageWidth - 150, 700, 100, 50); // Adjust x, y, width, height as needed
    }

    // Helper method to draw text on the PDF
    private float drawText(PDPageContentStream contentStream, PDType1Font font, int fontSize, String text, float x, float y) throws IOException {
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
        // Calculate the height of the text
        float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
        // Return the updated y position
        return y - textHeight;
    }

    // Helper method to draw location data on the PDF
    private float drawLocationData(PDPageContentStream contentStream, Iterable<ReportProjectsLocationDto> locationData, float x, float y, float lineHeight) throws IOException {
        for (ReportProjectsLocationDto locationDto : locationData) {
            String locationText = "Laboratory: " + locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%";
            y = drawText(contentStream, PDType1Font.HELVETICA, 12, locationText, x + 20, y);
            y -= lineHeight * 1.2;
        }
        return y;
    }

}