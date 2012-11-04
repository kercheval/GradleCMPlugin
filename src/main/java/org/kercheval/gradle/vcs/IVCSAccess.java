package org.kercheval.gradle.vcs;

import org.kercheval.gradle.util.SortedProperties;

import java.util.List;

//
// This interface supports the specific types of operation required by
// programmatic access to the VCS system in use.
//
// NOTE:  GIT is the only supported system at the moment.
//
public interface IVCSAccess {
    public enum Type { GIT }

    //
    // Get the current VCSType.
    //
    public Type getType();

    //
    // Obtain 'interesting' information about the current VCS usage
    // and return that as property information.
    //
    public SortedProperties getInfo();

    //
    // Get tags from repository.
    //
    public List<VCSTag> getAllTags();

    public List<VCSTag> getTags(String regexFilter);
}