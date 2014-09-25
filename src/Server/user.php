<?php

	// API for user functions: registration, user info, permissions, logins, etc

	$first = $_GET["first"];
	$last = $_GET["last"];
	$SSN = $_GET["ssn"];

	echo $first . "'s API key is " . generate_key($first, $last, "", "", "", $SSN, "");

	function generate_key($first, $last, $email, $phone, $birthday, $SSN, $accountType)
	{
		$userID = $last . $first . $SSN;
		$apiKey = md5($userID);
	
		return $apiKey;
	}
	
?>