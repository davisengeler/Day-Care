<?php
  include("config.php");

  // Connect to database
  $database = mysqli_connect(Database_HOST, Database_USER, Database_PASS, Database_NAME) or die("Could not connect to database");

  // What is the request for?
  if (isset($_GET['add']))
  {
    // Gets the child information from the request.
    $ssn = $_GET["ssn"];
    $firstName = $_GET["firstname"];
    $lastName = $_GET["lastname"];
    $dob = $_GET["dob"];
    $parentID = $_GET["parentid"];
    $classID = $_GET["classid"];

    if (mysqli_query($database, "CALL add_new_child($ssn, '$firstName', '$lastName', $dob, $parentID, $classID);"))
    {
      // New Request Submitted
      $response = array(
        "successful" => true,
        "statusMessage" => "The child has been added to the parent account."
        );

      echo json_encode($response);
    }
    else
    {
      // New Request Denied
      $response = array(
        "successful" => false,
        "statusMessage" => "Something was wrong with this request to add a child. " . $mysqlierror
        );

      echo json_encode($response);
    }
  }
  else if (isset($_GET['getnotes']))
  {
    $childIDs = array();
    $childIDs = json_decode($_GET['childids']);
    $noteList = array();


    // TODO: Echoing the errors as the loops goes could give an overall invalid JSON string, therefore not being decodable.
    foreach ($childIDs as $currentChild)
    {
      if ($result = mysqli_query($database, "CALL get_notes($currentChild);"))
      {
        // Gathers all the notes for the child
        while($row = mysqli_fetch_array($result))
        {
          $note = array();
          $note["ChildID"] = $currentChild;
          $note["NoteID"] = $row["NoteID"];
          $note["Message"] = $row["Message"];
          $note["SubjectID"] = $row["SubjectID"];
          $note["NoteType"] = $row["NoteType"];

          if ($note["NoteID"] != null)
          {
            $noteList[] = $note;
          }
        }
      }
    }
  }

  echo json_encode($noteList);

  function generateError($message)
  {
    $response = array(
      "successful" => false,
      "statusMessage" => $message
      );

    return $response;
  }
?>
