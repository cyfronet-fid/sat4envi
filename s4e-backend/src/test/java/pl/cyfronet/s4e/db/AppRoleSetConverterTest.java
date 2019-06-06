package pl.cyfronet.s4e.db;

import org.junit.jupiter.api.Test;
import pl.cyfronet.s4e.bean.AppRole;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppRoleSetConverterTest {
    @Test
    public void shouldConvertEntitySize1ToColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        String column = converter.convertToDatabaseColumn(Set.of(AppRole.CAT1));

        assertThat(column, is(equalTo("CAT1")));
    }

    @Test
    public void shouldConvertEntitySize2ToColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        String column = converter.convertToDatabaseColumn(Set.of(AppRole.CAT1, AppRole.CAT2));

        assertThat(column, anyOf(equalTo("CAT1,CAT2"), equalTo("CAT2,CAT1")));
    }

    @Test
    public void shouldConvertColumnValueToEntity() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        Set<AppRole> appRoles = converter.convertToEntityAttribute(AppRole.CAT1.name()+","+AppRole.CAT2.name());

        assertThat(appRoles, containsInAnyOrder(AppRole.CAT1, AppRole.CAT2));
    }

    @Test
    public void shouldHandleConvertIllegalColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("blah"));
    }

    @Test
    public void shouldHandleConvertWhitespaceColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        Set<AppRole> appRoles = converter.convertToEntityAttribute(" ");

        assertThat(appRoles, notNullValue());
        assertThat(appRoles, hasSize(0));
    }

    @Test
    public void shouldHandleConvertEmptyColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        Set<AppRole> appRoles = converter.convertToEntityAttribute("");

        assertThat(appRoles, notNullValue());
        assertThat(appRoles, hasSize(0));
    }

    @Test
    public void shouldHandleConvertNullColumnValue() {
        AppRoleSetConverter converter = new AppRoleSetConverter();

        Set<AppRole> appRoles = converter.convertToEntityAttribute(null);

        assertThat(appRoles, notNullValue());
        assertThat(appRoles, hasSize(0));
    }
}
