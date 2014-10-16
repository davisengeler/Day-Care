<?php

  //include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  $con = mysql_connect(DB_HOST,DB_USER,DB_PASS);
  if (!$con)
  {
    die('Could not connect to database: ' . mysql_error());
  }
  mysql_select_db(DB_NAME, $con);

  echo "HEY THEREs";

  $deviceID = $_GET["deviceID"];

  if(mysql_query("INSERT INTO Devices(DeviceID) VALUES ($deviceID)"))
  {
    echo "SHIT'S DONE";
  }
  else
  {
    echo "NOPE";
  }

?>
