<?php

  // Default Functions

  include("config.php");

  function db_connect()
  {

    $con = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME);
    if (!$con)
    {
      die('Could not connect to database: ' . mysql_error());
    }
    
  }

?>
