package org.sm.lab.mybooks.security;

import org.sm.lab.mybooks.domain.Reader;
import org.sm.lab.mybooks.enums.SystemRole;
import org.springframework.security.core.authority.AuthorityUtils;

public class UserDetailsImpl extends org.springframework.security.core.userdetails.User {

	private static final long serialVersionUID = 1L;

	private Long id;
	private SystemRole systemRole;
	private String displayName;
	
    public UserDetailsImpl(Reader reader) {
        super(reader.getEmail(), reader.getPassword(), AuthorityUtils.createAuthorityList(reader.getSystemRole().toString()));
        this.id = reader.getId();
        this.systemRole = reader.getSystemRole();
        setDisplayName(reader);
    }

    public Long getId() {
        return id;
    }

    public SystemRole getSystemRole() {
        return systemRole;
    }
    
    public String getDisplayName() {
    	return displayName;
    }
    
    private void setDisplayName(Reader reader) {
    	displayName = reader.getUsername();
    }

}
