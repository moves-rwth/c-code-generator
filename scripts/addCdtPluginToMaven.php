<?php

$cdtPluginFolder = "P:/Dev/cdt/plugins/";
$libFolder = "../CodeParser/lib/";

if ($argc < 2) {
	echo "Missing argument: Path to plugin jar!\r\n";
	exit(1);
}

$pluginPath = $argv[1];
$pluginName = basename($argv[1]);

$pomTemplate = <<<EOF
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns="http://maven.apache.org/POM/4.0.0"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <modelVersion>4.0.0</modelVersion>
  <groupId>TAG_GROUPID</groupId>
  <artifactId>TAG_ARTIFACTID</artifactId>
  <version>TAG_VERSION</version>
</project>
EOF;

$mavenTemplate = <<<EOF
<dependency>
	<groupId>TAG_GROUPID</groupId>
	<artifactId>TAG_ARTIFACTID</artifactId>
	<version>TAG_VERSION</version>
</dependency>	
EOF;

if (preg_match('/^([^_]+)_(.+).jar$/im', $pluginName, $regs)) {
	$names = $regs[1];
	$version = $regs[2];
	
	$namesArray = explode('.', $names);
	// First two into path, rest in artifact name
	$maxIndex = min(1, count($namesArray) - 2);
	$subPath = $namesArray[0];
	$groupId = $namesArray[0];
	for ($i = 1; $i <= $maxIndex; ++$i) {
		$subPath .= '/'.$namesArray[$i];
		$groupId .= '.'.$namesArray[$i];
	}
	
	$artifactName = $namesArray[$maxIndex + 1];
	for ($i = $maxIndex + 2; $i < count($namesArray); ++$i) {
		$artifactName .= '.'.$namesArray[$i];
	}
	
	echo "Using sub-path '".$subPath."' and artifact name '".$artifactName."' with version '".$version."'.\r\n";
	if (is_dir($libFolder.$subPath)) {
		echo "Sub-path exists, not creating.\r\n";
	} else if (!mkdir($libFolder.$subPath, 0777, true)) {
		echo "Error: Failed to create folders for sub-path!\r\n";
		exit(1);
	} else {
		echo "Created path for sub-path.\r\n";
	}
	
	if (is_dir($libFolder.$subPath.'/'.$artifactName.'/'.$version)) {
		echo "Artifact/version directory exists, not creating.\r\n";
	} else if (!mkdir($libFolder.$subPath.'/'.$artifactName.'/'.$version, 0777, true)) {
		echo "Error: Failed to create folders for artifact/version!\r\n";
		exit(1);
	} else {
		echo "Created path for artifact/version.\r\n";
	}
	
	if (!copy($pluginPath, $libFolder.$subPath.'/'.$artifactName.'/'.$version.'/'.$artifactName.'-'.$version.'.jar')) {
		echo "Error: Failed to copy jar to target location!\r\n";
		exit(1);
	} else {
		echo "Copied jar to target location.\r\n";
	}
	
	$pomContents = $pomTemplate;
	$pomContents = str_replace('TAG_GROUPID', $groupId, $pomContents);
	$pomContents = str_replace('TAG_ARTIFACTID', $artifactName, $pomContents);
	$pomContents = str_replace('TAG_VERSION', $version, $pomContents);
	file_put_contents($libFolder.$subPath.'/'.$artifactName.'/'.$version.'/'.$artifactName.'-'.$version.'.pom', $pomContents);
	
	echo "Success!\r\n";
	
	echo "For Maven:\r\n";
	$mavenContents = $mavenTemplate;
	$mavenContents = str_replace('TAG_GROUPID', $groupId, $mavenContents);
	$mavenContents = str_replace('TAG_ARTIFACTID', $artifactName, $mavenContents);
	$mavenContents = str_replace('TAG_VERSION', $version, $mavenContents);
	echo $mavenContents."\r\n";
} else {
	echo "Error: Did not understand file name!\r\n";
	exit(1);
}

echo "Done.\r\n";

function startsWith( $haystack, $needle ) {
     $length = strlen( $needle );
     return substr( $haystack, 0, $length ) === $needle;
}

function endsWith( $haystack, $needle ) {
    $length = strlen( $needle );
    if( !$length ) {
        return true;
    }
    return substr( $haystack, -$length ) === $needle;
}
