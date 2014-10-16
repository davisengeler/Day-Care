<?php

  // Default Functions

  function connect()
  {
    $con = mysql_connect(DB_HOST,DB_USER,DB_PASS);
    if (!$con)
    {
      die('Could not connect to database: ' . mysql_error());
    }
    mysql_select_db(DB_NAME, $con);
  }

?>
