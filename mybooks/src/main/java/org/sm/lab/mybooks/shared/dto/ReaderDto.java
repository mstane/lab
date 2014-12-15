package org.sm.lab.mybooks.shared.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ReaderDto implements Serializable{
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String username;
	private String password;
	private String firstName;
	private String lastName;
	private String email;
	

    private List<BookDto> books = new ArrayList<BookDto>(0);
	
	private int sessionDuration = 0;
	
	public ReaderDto() {
		
	}
	
	public ReaderDto(Long id, String username, String password, String firstName,
			String lastName, String email, List<BookDto> books) {
		super();
		this.id = id;
		this.username = username;
		this.password = password;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.books = books;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}

    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }	
    
    public List<BookDto> getBooks() {
        return books;
    }

    public void setBooks(List<BookDto> books) {
        this.books = books;
    }
	
	public int getSessionDuration() {
	    return this.sessionDuration;
	}

	public void setSessionDuration(int sessionDuration) {
	    this.sessionDuration = sessionDuration;
	}


}