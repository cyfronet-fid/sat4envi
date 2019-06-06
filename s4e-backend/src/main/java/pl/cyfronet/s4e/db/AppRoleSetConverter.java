package pl.cyfronet.s4e.db;

import pl.cyfronet.s4e.bean.AppRole;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Converter(autoApply = true)
public class AppRoleSetConverter implements AttributeConverter<Set<AppRole>, String> {
    private final static String SEP = ",";

    @Override
    public String convertToDatabaseColumn(Set<AppRole> appRoleSet) {
        return String.join(
                SEP,
                appRoleSet.stream()
                    .map(role -> role.name())
                    .collect(Collectors.toList())
        );
    }

    @Override
    public Set<AppRole> convertToEntityAttribute(String column) {
        if (column == null || column.trim().isEmpty()) {
            return Collections.emptySet();
        }
        return Arrays.asList(column.split(SEP)).stream()
                .map(segment -> AppRole.valueOf(segment))
                .collect(Collectors.toSet());
    }
}
