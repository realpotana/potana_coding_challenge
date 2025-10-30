import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;

public class TestCsvParsing {
    public static void main(String[] args) throws Exception {
        CsvMapper csvMapper = new CsvMapper();
        csvMapper.registerModule(new JavaTimeModule());
        
        CsvSchema schema = CsvSchema.emptySchema().withHeader();
        
        MappingIterator<CanonicalTrade> iterator = csvMapper
            .readerFor(CanonicalTrade.class)
            .with(schema)
            .readValues(new File("samples/sample-trades.csv"));
            
        while (iterator.hasNext()) {
            CanonicalTrade trade = iterator.next();
            System.out.println("Parsed: " + trade.getAccountNumber() + " - " + trade.getTimestamp());
        }
    }
}