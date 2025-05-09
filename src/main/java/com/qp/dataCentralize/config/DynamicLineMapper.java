package com.qp.dataCentralize.config;

import com.qp.dataCentralize.entity.Datas;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

public class DynamicLineMapper implements LineMapper<Datas> {

    private DefaultLineMapper<Datas> lineMapper;

    public DynamicLineMapper() {
        this.lineMapper = new DefaultLineMapper<>();
    }

    @Override
    public Datas mapLine(String line, int lineNumber) throws Exception {
        if (lineNumber == 0) {
            // Read the header row and set the column names
            String[] headers = line.split(",");
            DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
            lineTokenizer.setDelimiter(",");
            lineTokenizer.setNames(headers);

            BeanWrapperFieldSetMapper<Datas> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
            fieldSetMapper.setTargetType(Datas.class);

            lineMapper.setLineTokenizer(lineTokenizer);
            lineMapper.setFieldSetMapper(fieldSetMapper);
        }

        // Use the tokenizer and mapper to map the line
        return lineMapper.mapLine(line, lineNumber);
    }
}
