@REM 09-Nov-2012 1:13:24 pm
@REM 
@REM This file creates a simple git repository for testing purpose
@REM (see build.gradle for targets using this test repository)
@REM 
git init
mkdir gitfiles
echo foo > gitfiles/foo.txt
echo bar > gitfiles/bar.txt
git add gitfiles
git commit -a -m "Initial Checkin"
