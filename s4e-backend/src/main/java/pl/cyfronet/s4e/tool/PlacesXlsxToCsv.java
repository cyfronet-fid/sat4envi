package pl.cyfronet.s4e.tool;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import pl.cyfronet.s4e.bean.Place;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Converts miejscowosci.xlsx from PRNG to a CSV format.
 * <p>
 * The file miejscowosci.xlsx which can be obtained from the GUGiK site is available either in GML, SHAPEFILE or XLSX
 * format. I chose to use XLSX, which can be converted to a tab separated CSV with this tool.
 * <p>
 * The CSV file may have to be regenerated in case the register is out of date.
 * The result file should be placed in src/main/resources/db/places.csv.
 * <p>
 * To use it, pass the source xlsx file and the target csv file path. If the target file exists it will be overwritten.
 * <p>
 * The xlsx file is assumed to have following structure.
 * It starts with a header row, which is discarded.
 * The columns are assumed to be (columns indexed from 0):
 * <ul>
 *     <li>nazwaGlowna (name) - col 1</li>
 *     <li>rodzajObiektu  (type) - col 2</li>
 *     <li>szerokoscGeograficzna (latitude) - col 14</li>
 *     <li>dlugoscGeograficzna (longitude) - col 15</li>
 *     <li>wojewodztwo (voivodeship) - col 43</li>
 * </ul>
 * <p>
 * The output CSV file starts with a header with the names of the listed columns followed by the records.
 * The latitude and longitude are converted to double from 51°02'07'' format.
 *
 */
@Slf4j
public class PlacesXlsxToCsv {

    public static void main(String[] args) {
        new PlacesXlsxToCsv().run(args);
    }

    public void run(String[] args) {

        if (args.length != 2) {
            log.error("Usage: <source xlsx> <target csv>");
            System.exit(1);
        }

        String sourceXlsxName = args[0];
        String targetCsvName = args[1];

        log.info("Converting from file '"+sourceXlsxName+"' to file '"+targetCsvName+"'");

        File sourceFile = new File(sourceXlsxName);
        File targetFile = new File(targetCsvName);

        int countCorrect = 0;
        int countIncorrect = 0;

        try (
                InputStream inputStream = new FileInputStream(sourceFile);
                PrintWriter printWriter = new PrintWriter(targetFile, StandardCharsets.UTF_8)
        ) {
            log.info("Starting reading workbook");
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            log.info("Finished reading workbook");
            XSSFSheet sheet = workbook.getSheetAt(0);

            printHeader(printWriter);

            for (val row: sheet) {
                if (row.getRowNum() == 0) {
                    continue;
                }

                try {
                    String rodzajObiektu = row.getCell(2).getStringCellValue();

                    if (!"wieś".equals(rodzajObiektu) && !"miasto".equals(rodzajObiektu)) {
                        continue;
                    }

                    val placeBuilder = Place.builder();

                    for (val cell : row) {
                        processCell(cell, placeBuilder);
                    }

                    printLine(printWriter, placeBuilder.build());

                    countCorrect++;
                } catch (Exception e) {
                    countIncorrect++;
                }
            }
            log.info("Correct rows: "+countCorrect+". Incorrect rows: "+countIncorrect);
        } catch (IOException e) {
            log.error("Couldn't read source file", e);
        }
    }

    private void printHeader(PrintWriter printWriter) {
        printWriter.printf("name\ttype\tlatitude\tlongitude\tvoivodeship\n");
    }

    private void printLine(PrintWriter printWriter, Place place) {
        printWriter.printf("%s\t%s\t%f\t%f\t%s\n",
                place.getName(),
                place.getType(),
                place.getLatitude(),
                place.getLongitude(),
                place.getVoivodeship());
    }

    private void processCell(Cell cell, Place.PlaceBuilder placeBuilder) {
        if (cell.getCellType().equals(CellType.STRING)) {
            String value = cell.getStringCellValue();

            switch (cell.getColumnIndex()) {
                case 1: // nazwa główna
                    placeBuilder.name(value);
                    break;
                case 2: // rodzaj obiektu
                    placeBuilder.type(value);
                    break;
                case 14: // szerokość geograficzna
                    placeBuilder.latitude(parseLatLon(value));
                    break;
                case 15: // długość geograficzna
                    placeBuilder.longitude(parseLatLon(value));
                    break;
                case 43: // województwo
                    placeBuilder.voivodeship(value);
                    break;
            }
        }
    }

    /**
     * @param value must be given the format: "51°02'07''"
     * @return double value
     */
    private double parseLatLon(String value) {
        final int first = Integer.parseInt(value.substring(0, 0 + 2));
        final int second = Integer.parseInt(value.substring(3, 3 + 2));
        final int third = Integer.parseInt(value.substring(6, 6 + 2));

        return 1. * first + 1./60. * second + 1./60./60. * third;
    }
}
