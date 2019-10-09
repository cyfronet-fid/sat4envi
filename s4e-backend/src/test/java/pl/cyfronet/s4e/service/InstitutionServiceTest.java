package pl.cyfronet.s4e.service;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.cyfronet.s4e.BasicTest;
import pl.cyfronet.s4e.bean.Group;
import pl.cyfronet.s4e.bean.Institution;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.ex.GroupCreationException;
import pl.cyfronet.s4e.ex.InstitutionCreationException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@BasicTest
@Slf4j
public class InstitutionServiceTest {

    @Autowired
    private InstitutionService institutionService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private InstitutionRepository institutionRepository;

    @BeforeEach
    public void setUp() {
        institutionRepository.deleteAll();
    }

    @Test
    public void shouldDeleteInstitutionAndGroup() throws InstitutionCreationException, GroupCreationException {
        Institution institution = Institution.builder().name("Instytycja 15").slug("instytucja-15").build();
        institutionService.save(institution);

        Group group = Group.builder().name("Group 15").slug("group-15").institution(institution).build();
        groupService.save(group);

        institutionService.delete("instytucja-15");

        val groupDB = groupService.getGroup("instytucja-15", "group-15");
        val institutionDB = institutionService.getInstitution("instytucja-15");

        assertThat(groupDB.isEmpty(), is(true));
        assertThat(institutionDB.isEmpty(), is(true));
    }
}
