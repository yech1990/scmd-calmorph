#!/usr/bin/perl

# ./move_package.pl srcƒtƒHƒ‹ƒ_ prev_packagename new_packagename dest
# ex. src> ./move_package.pl ../../SCMDProject/src scmd lab.cb.scmd .    

use File::Basename;
use File::Path;

$srcdir = $ARGV[0];
$prev_p = $ARGV[1];
$new_p = $ARGV[2];
$dest = $ARGV[3];

open(FILELIST, "find $srcdir -name \"*.java\" |");
$srcdir =~ s/\//\\\//g;
$srcdir =~ s/\./\\\./g;
while(<FILELIST>)
{
	$inputfile = $_;
	print $inputfile;
	($rpath = $inputfile) =~ s/$srcdir\///;
	($n, $pname, $d) = fileparse($rpath);
	$pname =~ s/\//./g; 
	($new_pname = $pname) =~ s/$prev_p/$new_p/;
	$newfile = create_directory_for_package($new_pname);
	open(FILE, $inputfile);
	open(OUT, ">$newfile/$n");
	print "write to $newfile\n";
	while(<FILE>)
	{
		if(/\/\/\s*\$Date/)
		{
			print OUT "// \$URL\$ \r\n";
			next;
		}
		if(/\/\/\s*\$Revision/)
		{
			print OUT "// \$LastChangedBy\$ \r\n";
			next;
        }
		# replace package name
		s/$prev_p/$new_p/g;
		print OUT;
	}
	close(FILE);
	close(OUT);
}
close(FILELIST);



sub create_directory_for_package
{
	$p = shift;
	($path = $p) =~ s/\./\//g;
	$path = "$dest/$path";
	mkpath $path;
	return $path;
}
