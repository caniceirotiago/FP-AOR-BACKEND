package aor.fpbackend.utils;

import aor.fpbackend.dto.Report.ReportProjectsLocationDto;
import aor.fpbackend.dto.Report.ReportSummaryDto;
import jakarta.ejb.Stateless;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.IOException;

@Stateless
public class PdfGenerator {

    public void generateProjectReport(ReportSummaryDto reportSummary, String dest) throws IOException {
        // Create a new document
        PDDocument document = new PDDocument();
        // Create a new page
        PDPage page = new PDPage();
        document.addPage(page);
        // Start a new content stream
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        // Set up starting position for text
        float yPosition = 700; // Adjust this value as needed for vertical spacing
        // Title
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Project Report Summary");
        contentStream.endText();
        yPosition -= 40; // Move down for the next line

        // Average Members per Project
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Average Members per Project: " + reportSummary.getAverageMembersPerProject().getAverage());
        contentStream.endText();
        yPosition -= 40;

        // Average Project Duration
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Average Project Duration: " + reportSummary.getAverageProjectDuration().getAverage());
        contentStream.endText();
        yPosition -= 40;

        // Project Count by Location
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Project Count by Location:");
        contentStream.endText();
        yPosition -= 40;

        for (ReportProjectsLocationDto locationDto : reportSummary.getProjectCountByLocation()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(120, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= 40;
        }

        // Approved Projects by Location
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Approved Projects by Location:");
        contentStream.endText();
        yPosition -= 40;

        for (ReportProjectsLocationDto locationDto : reportSummary.getApprovedProjectsByLocation()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(120, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= 40;
        }

        // Completed Projects by Location
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Completed Projects by Location:");
        contentStream.endText();
        yPosition -= 40;

        for (ReportProjectsLocationDto locationDto : reportSummary.getCompletedProjectsByLocation()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(120, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= 40;
        }

        // Canceled Projects by Location
        contentStream.beginText();
        contentStream.newLineAtOffset(100, yPosition);
        contentStream.showText("Canceled Projects by Location:");
        contentStream.endText();
        yPosition -= 40;

        for (ReportProjectsLocationDto locationDto : reportSummary.getCanceledProjectsByLocation()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(120, yPosition);
            contentStream.showText(locationDto.getLocation().toString() + ": " + locationDto.getProjectCount() + " projects, " + locationDto.getProjectPercentage() + "%");
            contentStream.endText();
            yPosition -= 40;
        }

        // Close the content stream
        contentStream.close();

        // Save the document
        document.save(dest);

        // Close the document
        document.close();
    }
}