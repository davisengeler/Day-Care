// API for user functions: registration, user info, permissions, logins, etc

$first = $_GET["first"];
$last = $_GET["last"];
$SSN = $_GET["ssn"];

echo $first . "'s API key is " . create_user($first, $last, "", "", "", $SSN, "");

function create_user($first, $last, $email, $phone, $birthday, $SSN, $accountType)
{
	$userID = $last . $first;
	$apiKey = md5($userID);
	
	return $apiKey;
}