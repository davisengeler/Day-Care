<?php

  include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  // Gets the device ID from the request.
  $deviceID = $_GET["deviceID"];


  // TODO: Add verification logic for whoever requested the user creation
  // TODO: change _GET to _SET
  $ssn = $_GET["ssn"];
  $firstName = $_GET["firstname"];
  $lastName = $_GET["lastname"];
  $address = $_GET["address"];
  $phone = $_GET["phone"];
  $email = $_GET["email"];
  $pass = $_GET["pass"];
  $accID = $_GET["accid"];


  if ($result = mysql_query("INSERT INTO Account(SSN, FirstName, LastName, Address, Phone, Email, Pass, AccID) VALUES ('$ssn', '$firstName', '$lastName','$address','$phone','$email','$pass','$accID', 'verified');"))
  {
    // New Request Submitted
    echo "You have requested authentication for " . $firstName . " " . $lastName;
  }
  else
  {
    // New Request Denied
    echo "Couldn't request authentication: " . mysql_error();
  }

?>
