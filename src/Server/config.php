<?php

  // Database Configuration

  define("Database_NAME", "davisengeler_daycare");
  define("Database_HOST", "localhost");
  define("Database_USER", "daycare");
  define("Database_PASS", "engelerhetzel");

  function connectDB()
  {
    $connection = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");
    return $connection;
  }

?>
