package org.kercheval.gradle.vcs;

import org.kercheval.gradle.util.SortedProperties;

//
// This interface supports the specific types of operation required by
// programmatic access to the VCS system in use.
//
// NOTE:  GIT is the only supported system at the moment.
//
public interface IVCSAccess {
    public enum VCSType { GIT }

    //
    // Get the current VCSType.
    //
    public VCSType getVCSType();

    //
    // Obtain 'interesting' information about the current VCS usage
    // and return that as property information.
    //
    public SortedProperties getVCSInfo();
}
