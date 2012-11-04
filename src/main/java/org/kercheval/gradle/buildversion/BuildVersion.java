package org.kercheval.gradle.buildversion;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.Date;

public class BuildVersion {

    //
    // Pattern used for date/time (not modifiable)
    //
    public static final String DATE_FORMAT_PATTERN = "yyyyMMdd";
    public static final String TIME_FORMAT_PATTERN = "HHmmss";

    //
    // The default pattern uses major, minor and standard maven time format
    //
    public static final String DEFAULT_PATTERN = "%M%.%m%-%d%.%t%";

    //
    // The actual version info pulled from the candidate based on the
    // pattern or passed into the constructor
    //
    private int major = 0;
    private int minor = 0;
    private int build = 0;
    private Date buildDate = null;

    //
    // These values are set based on the presence of variables in the
    // pattern in use.  Set in validatePattern.
    //
    private boolean useMajor = false;
    private boolean useMinor = false;
    private boolean useBuild = false;

    //
    // Pattern used for version.  The pattern must be set at follows...
    // - May not have any whitespace (validated)
    // - May contain any of the following variables (at most once)
    // %M% - major version
    // %m% - minor version
    // %b% - build number
    // %d% - date (using yyyyMMdd)
    // %t% - time (using HHmmss)
    // %% - a percent
    //
    private final String pattern;

    //
    // Create a default version
    //
    public BuildVersion(final String pattern, final String candidate) {
        if (null != pattern) {
            this.pattern = pattern;
        } else {
            this.pattern = DEFAULT_PATTERN;
        }

        init(0, 0, 0, null);
        parseCandidate(candidate);
    }

    public BuildVersion(final String pattern, final int major, final int minor, final int build, final Date buildDate) {
        if (null != pattern) {
            this.pattern = pattern;
        } else {
            this.pattern = DEFAULT_PATTERN;
        }

        init(major, minor, build, buildDate);
    }

    @SuppressWarnings("hiding")
    private void init(final int major, final int minor, final int build, final Date buildDate) {
        this.major = major;
        this.minor = minor;
        this.build = build;
        this.buildDate = buildDate;

        if (null == buildDate) {
            this.buildDate = new Date();
        }

        validatePattern();
    }

    private void validatePattern() {
        final String validatePattern = getPattern();

        //
        // Ensure the pattern contains no whitespace
        //
        if (validatePattern.matches("\\S*\\s+\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: whitespace not allowed in pattern");
        }

        //
        // Ensure each pattern type is used zero or one times only
        //
        if (validatePattern.matches("\\S*%M%\\S*%M%\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: Major variable %M% used more than once in pattern");
        }

        if (validatePattern.matches("\\S*%m%\\S*%m%\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: Minor variable %m% used more than once in pattern");
        }

        if (validatePattern.matches("\\S*%b%\\S*%b%\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: Build variable %b% used more than once in pattern");
        }

        if (validatePattern.matches("\\S*%d%\\S*%d%\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: Date variable %d% used more than once in pattern");
        }

        if (validatePattern.matches("\\S*%t%\\S*%t%\\S*")) {
            throw new IllegalArgumentException("Invalid pattern: Time variable %t% used more than once in pattern");
        }

        //
        // Validate the escape/variable syntax is used correctly and set the usage booleans
        //
        int index = 0;

        while (index >= 0) {

            //
            // Find the next pattern block to validate
            //
            index = validatePattern.indexOf("%", index);

            if (index >= 0) {

                //
                // The string must have enough space left for either another % or a variable
                // followed by a %
                //
                if (validatePattern.length() == index + 1) {
                    throw new IllegalArgumentException("Invalid pattern: unbalanced % found at end of pattern");
                }

                if ((validatePattern.length() == index + 2) && (validatePattern.charAt(index + 1) != '%')) {
                    throw new IllegalArgumentException("Invalid pattern: unbalanced % found at end of pattern");
                }

                final char nextChar = validatePattern.charAt(index + 1);

                if (nextChar == '%') {
                    index += 2;
                } else {
                    final char fenceChar = validatePattern.charAt(index + 2);

                    if (fenceChar != '%') {
                        throw new IllegalArgumentException(
                            "Invalid pattern: invalid variable reference at pattern index " + (index + 2));
                    }

                    switch (nextChar) {
                    case 'M' :
                        setUseMajor(true);

                        break;

                    case 'm' :
                        setUseMinor(true);

                        break;

                    case 'b' :
                        setUseBuild(true);

                        break;

                    case 'd' :
                    case 't' :
                        break;

                    default :
                        throw new IllegalArgumentException("Invalid pattern: invalid variable reference '" + nextChar
                                                           + "' at pattern index " + (index + 1));
                    }

                    index += 3;
                }
            }
        }
    }

    private void setUseMajor(final boolean useMajor) {
        this.useMajor = useMajor;
    }

    private void setUseMinor(final boolean useMinor) {
        this.useMinor = useMinor;
    }

    private void setUseBuild(final boolean useBuild) {
        this.useBuild = useBuild;
    }

    private void parseCandidate(final String candidate) {
        if (null != candidate) {

            //
            // These values are extracted during parse and pulled together to make a
            // valid date at the end of the parse.
            //
            String dateStr = "";
            String timeStr = "";

            //
            // Note that the build date can only be derived if the 'date' portion of the
            // pattern is set.  The derived date if that date pattern is not present will be
            // 'now'.  If the time pattern variable is not present, the date will be midnight
            // of the date in the candidate.
            //
            final String parsePattern = getPattern();

            //
            // The pattern is known valid, just fill in the blanks
            //
            int patternIndex = 0;
            int candidateIndex = getNextNumberIndex(candidate, 0);

            while (patternIndex >= 0) {

                //
                // Find the next pattern block to validate
                //
                patternIndex = parsePattern.indexOf("%", patternIndex);

                int nextPatternIndex = patternIndex + 3;
                int nextCandidateIndex = candidateIndex;
                int nextInt = 0;

                if (patternIndex >= 0) {
                    final char nextChar = parsePattern.charAt(patternIndex + 1);

                    switch (nextChar) {
                    case '%' :

                        //
                        // backup the index one since this is only 2 characters consumed
                        //
                        nextPatternIndex -= 1;

                        break;

                    case 'M' :
                        nextCandidateIndex = getNextNonNumberIndex(candidate, candidateIndex);

                        if (candidateIndex != nextCandidateIndex) {
                            nextInt = Integer.valueOf(candidate.substring(candidateIndex, nextCandidateIndex));
                        }

                        setMajor(nextInt);

                        break;

                    case 'm' :
                        nextCandidateIndex = getNextNonNumberIndex(candidate, candidateIndex);

                        if (candidateIndex != nextCandidateIndex) {
                            nextInt = Integer.valueOf(candidate.substring(candidateIndex, nextCandidateIndex));
                        }

                        setMinor(nextInt);

                        break;

                    case 'b' :
                        nextCandidateIndex = getNextNonNumberIndex(candidate, candidateIndex);

                        if (candidateIndex != nextCandidateIndex) {
                            nextInt = Integer.valueOf(candidate.substring(candidateIndex, nextCandidateIndex));
                        }

                        setBuild(nextInt);

                        break;

                    case 'd' :
                        nextCandidateIndex = getNextNonNumberIndex(candidate, candidateIndex);

                        if (candidateIndex != nextCandidateIndex) {
                            dateStr = candidate.substring(candidateIndex, nextCandidateIndex);
                        }

                        break;

                    case 't' :
                        nextCandidateIndex = getNextNonNumberIndex(candidate, candidateIndex);

                        if (candidateIndex != nextCandidateIndex) {
                            timeStr = candidate.substring(candidateIndex, nextCandidateIndex);
                        }

                        break;

                    default :

                        //
                        // This state is not possible if the validate method works.  Not testable
                        // without breaking private contract
                        //
                        throw new IllegalStateException("Invalid pattern detected '" + getPattern() + "' at index: "
                                                        + patternIndex);
                    }

                    patternIndex = nextPatternIndex;
                    candidateIndex = getNextNumberIndex(candidate, nextCandidateIndex);
                }
            }

            //
            // Last step is to formulate the date if possible
            //
            if (dateStr.length() > 0) {
                final SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN + "."
                                                       + TIME_FORMAT_PATTERN);

                formatter.setLenient(true);

                try {
                    setBuildDate(formatter.parse(dateStr + "." + timeStr));
                } catch (final ParseException e) {

                    //
                    // We got a bogus date out of our candidate string.  Ignore it...
                    //
                }
            }
        }
    }

    private int getNextNumberIndex(final String candidate, final int startIndex) {
        int currentIndex = startIndex;

        while ((currentIndex < candidate.length()) &&!Character.isDigit(candidate.charAt(currentIndex))) {
            currentIndex++;
        }

        return currentIndex;
    }

    private int getNextNonNumberIndex(final String candidate, final int startIndex) {
        int currentIndex = startIndex;

        while ((currentIndex < candidate.length()) && Character.isDigit(candidate.charAt(currentIndex))) {
            currentIndex++;
        }

        return currentIndex;
    }

    public String getPattern() {
        return pattern;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(final int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(final int minor) {
        this.minor = minor;
    }

    public int getBuild() {
        return build;
    }

    public void setBuild(final int build) {
        this.build = build;
    }

    public Date getBuildDate() {
        return buildDate;
    }

    public void setBuildDate(final Date buildDate) {
        this.buildDate = buildDate;
    }

    public boolean useMajor() {
        return this.useMajor;
    }

    public boolean useMinor() {
        return this.useMinor;
    }

    public boolean useBuild() {
        return this.useBuild;
    }

    //
    // This method increments the build version in the most 'natural' way.
    // The build number is considered the most volatile, followed by the minor
    // version and finally followed by the major version.  The date is always updated
    // as a result of the increment of the build version.
    //
    public void incrementVersion() {
        if (useBuild()) {
            incrementBuild();
        } else if (useMinor()) {
            incrementMinor();
        } else if (useMajor()) {
            incrementMajor();
        } else {
            updateDate();
        }
    }

    public void incrementBuild() {
        if (useBuild()) {
            build++;
        }

        updateDate();
    }

    public void incrementMinor() {
        if (useMinor()) {
            minor++;
        }

        updateDate();
    }

    public void incrementMajor() {
        if (useMajor()) {
            major++;
        }

        updateDate();
    }

    public void updateDate() {
        setBuildDate(new Date());
    }

    @Override
    public String toString() {
        final StringBuilder versionStr = new StringBuilder();
        final String buildPattern = getPattern();

        //
        // The pattern is known valid, just fill in the blanks
        //
        int index = 0;
        int lastIndex = index;

        while (index >= 0) {

            //
            // Find the next pattern block to validate
            //
            index = buildPattern.indexOf("%", index);

            int nextIndex = index + 3;

            if (index >= 0) {

                //
                // Place the block from the last match to the current into the string
                //
                versionStr.append(buildPattern.substring(lastIndex, index));

                final char nextChar = buildPattern.charAt(index + 1);

                switch (nextChar) {
                case '%' :
                    versionStr.append("%");

                    //
                    // Backup the index one since this is only 2 characters consumed
                    //
                    nextIndex -= 1;

                    break;

                case 'M' :
                    versionStr.append(getMajor());

                    break;

                case 'm' :
                    versionStr.append(getMinor());

                    break;

                case 'b' :
                    versionStr.append(getBuild());

                    break;

                case 'd' :
                    final SimpleDateFormat dateFormatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);

                    versionStr.append(dateFormatter.format(getBuildDate()));

                    break;

                case 't' :
                    final SimpleDateFormat timeFormatter = new SimpleDateFormat(TIME_FORMAT_PATTERN);

                    versionStr.append(timeFormatter.format(getBuildDate()));

                    break;

                default :

                    //
                    // This state is not possible if the validate method works.  Not testable
                    // without breaking private contract
                    //
                    throw new IllegalStateException("Invalid pattern detected '" + getPattern() + "' at index: "
                                                    + index);
                }

                index = nextIndex;
                lastIndex = index;
            }
        }

        //
        // Tack on the postfix (if any)
        //
        versionStr.append(buildPattern.substring(lastIndex));

        return versionStr.toString();
    }
}
