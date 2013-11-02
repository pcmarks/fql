package fql.examples;

public class PeopleExample extends Example {

	@Override
	public String getName() {
		return "People";
	}

	@Override
	public String getText() {
		return s;
	}

	String s = "/* Here we have males and females, each with first and last names, living in a dwelling in a city.*/"
			+ "\nschema MalesAndFemales = { "
			+ "\n nodes"
			+ "\n 	Male, Female, Dwelling, City;"
			+ "\n attributes"
			+ "\n 	DwellingDesc:Dwelling->string,"
			+ "\n	CityName:City->string,"
			+ "\n	FirstNameM:Male->string,"
			+ "\n	LastNameM:Male->string,"
			+ "\n	FirstNameF:Female->string,"
			+ "\n	LastNameF:Female->string;"
			+ "\n arrows"
			+ "\n	LivesM:Male->Dwelling,"
			+ "\n	LivesF:Female->Dwelling,"
			+ "\n	IsIn:Dwelling->City;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\n// We put in the simpsons, and the creators of FQL."
			+ "\ninstance I = {"
			+ "\n nodes"
			+ "\n	Male->{1,2,3,4,5},"
			+ "\n	Female->{1,2,3,4},"
			+ "\n	Dwelling->{1,2,3,4},"
			+ "\n	City->{1,2,3,4};"
			+ "\n attributes"
			+ "\n	CityName->{(1,Springfield),(2,Shelbyville),(3,Cambridge),(4,Somerville)},"
			+ "\n	FirstNameM->{(1,Homer),(2,Bart),(3,Ned),(4,David),(5,Ryan)},"
			+ "\n	LastNameM->{(1,Simpson),(2,Simpson),(3,Flanders),(4,Spivak),(5,Wisnesky)},"
			+ "\n	FirstNameF->{(1,Marge),(2,Lisa),(3,Maggie),(4,Maud)},"
			+ "\n	LastNameF->{(1,Simpson),(2,Simpson),(3,Simpson),(4,Flanders)},"
			+ "\n	DwellingDesc->{(1,House),(2,House),(3,SmallAptBldg),(4,LargeAptBldg)};"
			+ "\n arrows"
			+ "\n	LivesM->{(1,1),(2,1),(3,2),(4,3),(5,4)},"
			+ "\n	LivesF->{(1,1),(2,1),(3,1),(4,2)},"
			+ "\n	IsIn->{(1,1),(2,1),(3,4),(4,3)};"
			+ "\n} : MalesAndFemales"
			+ "\n	"
			+ "\n// Here we just have people with first names living in cities."
			+ "\nschema Basic = {"
			+ "\n nodes Person, City;"
			+ "\n attributes"
			+ "\n	FirstName:Person->string,"
			+ "\n	LastName:Person->string,"
			+ "\n	CityName:City->string;"
			+ "\n arrows"
			+ "\n 	LivesIn:Person->City;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\n// We'll attach Person to Female."
			+ "\nmapping JustFems = {"
			+ "\n nodes"
			+ "\n	Person->Female,"
			+ "\n	City->City;"
			+ "\n attributes"
			+ "\n	FirstName->FirstNameF,"
			+ "\n	LastName->LastNameF,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	LivesIn->Female.LivesF.IsIn;"
			+ "\n} : Basic -> MalesAndFemales"
			+ "\n"
			+ "\n//Get the information about Females."
			+ "\ninstance JustFemsI = delta JustFems I "
			+ "\n"
			+ "\n/*We don't plan to have any two cities with the same name, so we remove the key.*/"
			+ "\nschema VeryBasic = {"
			+ "\n nodes Person;"
			+ "\n attributes"
			+ "\n	FirstName:Person->string,"
			+ "\n	LastName:Person->string,"
			+ "\n	CityName:Person->string;"
			+ "\n arrows;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\n/*Combine the person and the city, so that CityName is an attribute of Person.*/"
			+ "\nmapping Simplify = {"
			+ "\n nodes"
			+ "\n	Person->Person,"
			+ "\n	City->Person;"
			+ "\n attributes"
			+ "\n	FirstName->FirstName,"
			+ "\n	LastName->LastName,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	LivesIn->Person;"
			+ "\n} : Basic -> VeryBasic"
			+ "\n"
			+ "\ninstance SimpleFemsI  = pi Simplify JustFemsI //A one-table summary of the females."
			+ "\n"
			+ "\n/*SimpleFemsI and SimpleFems2I should return the same thing*/"
			+ "\nmapping idVB:VeryBasic->VeryBasic = id VeryBasic"
			+ "\nquery FemSummary : MalesAndFemales -> VeryBasic = delta JustFems pi Simplify sigma idVB"
			+ "\ninstance SimpleFems2I : VeryBasic = eval FemSummary I"
			+ "\n"
			+ "\n/*We no longer care about gender, but we want all the data in MalesAndFemales (not just the females).*/"
			+ "\nschema People = {"
			+ "\n nodes"
			+ "\n 	Person, Dwelling, City;"
			+ "\n attributes"
			+ "\n	DwellingDesc:Dwelling->string,"
			+ "\n	FirstName:Person->string,"
			+ "\n	LastName:Person->string,"
			+ "\n	CityName:City->string;"
			+ "\n arrows"
			+ "\n 	Lives:Person->Dwelling,"
			+ "\n	IsIn:Dwelling->City;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\n	/*We throw females and males into the same table (Person).*/"
			+ "\nmapping UnionGenders = {"
			+ "\n nodes"
			+ "\n	Male->Person,"
			+ "\n	Female->Person,"
			+ "\n	Dwelling->Dwelling,"
			+ "\n	City->City;"
			+ "\n attributes"
			+ "\n	DwellingDesc->DwellingDesc,"
			+ "\n	FirstNameM->FirstName,"
			+ "\n	LastNameM->LastName,"
			+ "\n	FirstNameF->FirstName,"
			+ "\n	LastNameF->LastName,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	LivesM->Person.Lives,"
			+ "\n	LivesF->Person.Lives,"
			+ "\n	IsIn->Dwelling.IsIn;"
			+ "\n} : MalesAndFemales -> People"
			+ "\n"
			+ "\ninstance UnionGendersI = sigma UnionGenders I /*Apply sigma to union the genders.*/"
			+ "\n"
			+ "\n/*We no longer care about dwelling description; drop it.*/"
			+ "\nschema BasicPeople = {"
			+ "\n nodes"
			+ "\n 	Person, Dwelling, City;"
			+ "\n attributes"
			+ "\n	FirstName:Person->string,"
			+ "\n	LastName:Person->string,"
			+ "\n	CityName:City->string;"
			+ "\n arrows"
			+ "\n 	Lives:Person->Dwelling,"
			+ "\n	IsIn:Dwelling->City;"
			+ "\n equations;"
			+ "\n}"
			+ "\n"
			+ "\nmapping ForgetDwellingDesc = {"
			+ "\n nodes"
			+ "\n	Person->Person,"
			+ "\n	Dwelling->Dwelling,"
			+ "\n	City->City;"
			+ "\n attributes"
			+ "\n	FirstName->FirstName,"
			+ "\n	LastName->LastName,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	Lives->Person.Lives,"
			+ "\n	IsIn->Dwelling.IsIn;"
			+ "\n} : BasicPeople -> People"
			+ "\n"
			+ "\nmapping Simplify2 = {"
			+ "\n nodes"
			+ "\n	Person->Person,"
			+ "\n	Dwelling->Person,"
			+ "\n	City->Person;"
			+ "\n attributes"
			+ "\n	FirstName->FirstName,"
			+ "\n	LastName->LastName,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	Lives->Person,"
			+ "\n	IsIn->Person;"
			+ "\n} : BasicPeople -> VeryBasic"
			+ "\n"
			+ "\nquery q:People->VeryBasic = delta ForgetDwellingDesc pi Simplify2 sigma idVB"
			+ "\ninstance BasicPeopleI :VeryBasic = eval q UnionGendersI"
			+ "\n"
			+ "\n/*We now want a schema that shows pairs of males and females living together.*/"
			+ "\nschema Cohabitators = {"
			+ "\n nodes"
			+ "\n	Pair, Dwelling, City;"
			+ "\n attributes"
			+ "\n	DwellingDesc:Dwelling->string,"
			+ "\n	FirstNameM:Pair->string,"
			+ "\n	LastNameM:Pair->string,"
			+ "\n	FirstNameF:Pair->string,"
			+ "\n	LastNameF:Pair->string,"
			+ "\n	CityName:City->string;"
			+ "\n arrows"
			+ "\n	Lives:Pair->Dwelling,"
			+ "\n	IsIn:Dwelling->City;"
			+ "\n equations;"
			+ "\n}"
			+ "\n	"
			+ "\n/*We send males and females both to Pair.*/"
			+ "\nmapping BothGenders:MalesAndFemales->Cohabitators = {"
			+ "\n nodes"
			+ "\n	Male->Pair,"
			+ "\n	Female->Pair,"
			+ "\n	Dwelling->Dwelling,"
			+ "\n	City->City;"
			+ "\n attributes"
			+ "\n	DwellingDesc->DwellingDesc,"
			+ "\n	FirstNameM->FirstNameM,"
			+ "\n	LastNameM->LastNameM,"
			+ "\n	FirstNameF->FirstNameF,"
			+ "\n	LastNameF->LastNameF,"
			+ "\n	CityName->CityName;"
			+ "\n arrows"
			+ "\n	LivesM->Pair.Lives,"
			+ "\n	LivesF->Pair.Lives,"
			+ "\n	IsIn->Dwelling.IsIn;"
			+ "\n}"
			+ "\n"
			+ "\ninstance CohabitatorsI : Cohabitators = pi BothGenders I //We use pi to join the tables and see our cohabitators.";
}
