package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.InvitationRepository;
import pl.cyfronet.s4e.ex.InvitationCreationException;
import pl.cyfronet.s4e.util.AppUserDetailsSupplier;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvitationService {
    private final AppUserRepository appUserRepository;
    private final InstitutionRepository institutionRepository;
    private final InvitationRepository invitationRepository;

    @Transactional(rollbackFor = {InvitationCreationException.class, NotFoundException.class})
    public String createInvitationFrom(String email, String institutionSlug) throws InvitationCreationException, NotFoundException {
        val institution = institutionRepository.findBySlug(institutionSlug, Institution.class)
                .orElseThrow(() -> constructNFE("institution slug: " + institutionSlug));
        val optionalInvitation = invitationRepository.findByEmailAndInstitutionSlug(
                email,
                institutionSlug,
                Invitation.class
        );
        if (optionalInvitation.isPresent()) {
            throw new InvitationCreationException("Invitation already exists in DB");
        }

        val token = UUID.randomUUID().toString();
        val invitation = Invitation.builder()
                .email(email)
                .status(InvitationStatus.WAITING)
                .token(token)
                .institution(institution)
                .build();
        invitationRepository.save(invitation);

        return token;
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void confirmInvitation(String token) throws NotFoundException {
        val invitation = invitationRepository.findByToken(token, Invitation.class)
                .orElseThrow(() -> constructNFE("token: " + token));
        val institution = invitation.getInstitution();

        val user = appUserRepository.findByEmail(AppUserDetailsSupplier.get().getEmail())
                .orElseThrow(() -> constructNFE("token: " + token));
        addMemberToDefaultGroup(user, institution);
    }

    @Transactional(rollbackFor = NotFoundException.class)
    public void rejectInvitation(String token) throws NotFoundException {
        val invitation = invitationRepository.findByToken(token, Invitation.class)
                .orElseThrow(() -> constructNFE("token: " + token));
        invitation.setStatus(InvitationStatus.REJECTED);
    }

    public <T> Set<T> findAllBy(String institutionSlug, Class<T> projection) {
        return invitationRepository.findAllByInstitutionSlug(institutionSlug, projection);
    }

    public <T> Optional<T> findByIdAndInstitutionSlug(Long id, String institutionSlug, Class<T> projection) {
        return invitationRepository.findByIdAndInstitutionSlug(id, institutionSlug, projection);
    }

    public <T> Optional<T> findByEmailAndInstitutionSlug(String email, String institutionSlug, Class<T> projection) {
        return invitationRepository.findByEmailAndInstitutionSlug(email, institutionSlug, projection);
    }

    public <T> Optional<T> findInstitutionBy(String token, Class<T> projection) throws NotFoundException {
        val invitation = invitationRepository.findByToken(token, Invitation.class)
                .orElseThrow(() -> constructNFE("token: " + token));
        val institution = institutionRepository.findById(invitation.getInstitution().getId(), projection);
        return institution;
    }

    public <T> Optional<T> findByToken(String token, Class<T> projection) {
        return invitationRepository.findByToken(token, projection);
    }

    @Transactional
    public void deleteBy(String token) throws NotFoundException {
        val invitation = invitationRepository.findByToken(token, Invitation.class)
                .orElseThrow(() -> constructNFE("token: ", token));
        invitationRepository.delete(invitation);
    }

    private void addMemberToDefaultGroup(AppUser user, Institution institution) throws NotFoundException {
        val dbInstitution = institutionRepository.findBySlug(institution.getSlug(), Institution.class)
                .orElseThrow(() -> new NotFoundException("Institution not found for : " + institution.getSlug()));
        val sourceRoles = dbInstitution.getMembersRoles();
        val memberRole = UserRole
                .builder()
                .role(AppRole.INST_MEMBER)
                .user(user)
                .institution(dbInstitution)
                .build();
        if (!sourceRoles.contains(memberRole)) {
            dbInstitution.getMembersRoles().add(memberRole);
            user.getRoles().add(memberRole);
        }
    }

    private pl.cyfronet.s4e.ex.NotFoundException constructNFE(String... args) {
        return new NotFoundException("Invitation not found for '" + String.join(", ", args) + "'");
    }
}
