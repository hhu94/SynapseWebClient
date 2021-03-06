package org.sagebionetworks.web.shared;


import org.sagebionetworks.repo.model.JoinTeamSignedToken;
import org.sagebionetworks.repo.model.MembershipInvtnSignedToken;
import org.sagebionetworks.repo.model.SignedTokenInterface;
import org.sagebionetworks.repo.model.auth.NewUserSignedToken;
import org.sagebionetworks.repo.model.message.NotificationSettingsSignedToken;
import org.sagebionetworks.repo.model.principal.EmailValidationSignedToken;

public enum NotificationTokenType  {
	JoinTeam(JoinTeamSignedToken.class),
	NewUser(NewUserSignedToken.class),
	Settings(NotificationSettingsSignedToken.class),
	EmailValidation(EmailValidationSignedToken.class),
	EmailInvitation(MembershipInvtnSignedToken.class);

	public final Class<? extends SignedTokenInterface> classType;
	NotificationTokenType(Class<? extends SignedTokenInterface> classType) {
		this.classType = classType;
	}
}
