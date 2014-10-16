<?php

  //include("config.php");
  include("default-functions.php");

  // Connect to DB. Comes from default-functions.php
  db_connect();

  echo "HEY THEREs";

  $deviceID = $_GET["deviceID"];

  if(mysql_query("INSERT INTO Devices(DeviceID) VALUES ($deviceID);"))
  {
    echo "SHIT'S DONE";
  }
  else
  {
    echo "NOPE";
  }

?>
