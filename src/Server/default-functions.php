<?php

  // Default Functions

  include("config.php");

  function db_connect()
  {
    $con = mysql_connect(Database_HOST,Database_USER,Database_PASS);
    if (!$con)
    {
      die('Could not connect to database: ' . mysql_error());
    }
    mysql_select_db(Database_NAME, $con);
  }

?>
