package com.beijunyi.parallelgit.web.protocol.model;

import java.util.Date;
import javax.annotation.Nonnull;

import org.eclipse.jgit.lib.PersonIdent;

public class PersonView {

  private final String name;
  private final String email;
  private final Date timestamp;

  private PersonView(@Nonnull String name, @Nonnull String email, @Nonnull Date timestamp) {
    this.name = name;
    this.email = email;
    this.timestamp = timestamp;
  }

  @Nonnull
  public static PersonView of(@Nonnull PersonIdent person) {
    return new PersonView(person.getName(), person.getEmailAddress(), person.getWhen());
  }

  @Nonnull
  public String getName() {
    return name;
  }

  @Nonnull
  public String getEmail() {
    return email;
  }

  @Nonnull
  public Date getTimestamp() {
    return timestamp;
  }

}
