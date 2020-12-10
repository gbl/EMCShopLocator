#!/usr/bin/perl

use 5.1.0;
use CGI;
use DBI;
use DBD::mysql;

print qq(Content-type: text/plain\n);
print qq(\n);


my $cgi=CGI->new;
my $rows=0;
my $dbh;

if ($cgi->param("clientversion") ne "" && $cgi->param("upload")) {
	$dbh=DBI->connect("DBI:mysql:database=emcmarket;host=localhost", "emcmarket", "NE6xYRe6UPwW3Ray", {AutoCommit => 0 });
	my $name=$cgi->param("name");
	exit unless $name =~ /^[a-zA-Z0-9]+$/;
	$name=$&;
	open(DEBUG, ">/tmp/upload.debug/".$name.".".time);
	my $separator=":";
	if ($cgi->param("clientversion") ne "") {
		$separator="\\|";
	}
	my $sthsel=$dbh->prepare(qq(
		select count(*) from signs
		where server=? and x=? and y=? and z=? and chooseposition=? and lastseen > ?
	));
	my $sthdel=$dbh->prepare(qq(
		delete from signs
		where server=? and x=? and y=? and z=? and chooseposition=?
	));
	my $sthins=$dbh->prepare(qq(
		insert into signs (server, x, y, z, amount, buy, sell, owner, item, resnumber, chooseposition, lastseen, uploader, todelete)
		values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
	));
	foreach my $row (split("\n", $cgi->param("upload"))) {
		my ($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen, $todelete)=
			split($separator, $row);
		$choosepos="-1" if $choosepos eq "";
		$lastseen=time*1000 if $lastseen eq "";
		if ($todelete eq "todelete") { $todelete = 1; }
		else { $todelete=0; }
		$sthsel->execute($server, $x, $y, $z, $choosepos, $lastseen);
		if (((my $count)=$sthsel->fetchrow_array()) && $count>0) {
			print DEBUG "ignore $row\n";
		} else {
			$sthdel->execute($server, $x, $y, $z, $choosepos);
			$sthins->execute($server, $x, $y, $z, $amount, $buy, $sell, $owner, $item, $resnumber, $choosepos, $lastseen, $name, $todelete) or print "$row : ".$dbh->errstr."\n";
			print DEBUG "updated $row\n";
		}
		$rows++;
	}
	close DEBUG;
	$sthsel->finish;
	$sthins->finish;
	$sthdel->finish;
	$dbh->commit;
	open(F, ">>/tmp/uploaders.dat");
	print F scalar localtime, "  ",  $rows ," uploaded by ", scalar $cgi->param("name"), " client ", scalar $cgi->param("clientversion"), "\n";
	close F;
	$dbh->disconnect;
}

print "$rows rows uploaded.\n";
