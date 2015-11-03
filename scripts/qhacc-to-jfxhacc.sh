#!/bin/bash

if [ $# -lt 1 ]; then
  echo "Syntax: $0 <qhacc db>"
  exit 1
fi

SQLITE3=sqlite3
DB=$1
TMPDIR=/tmp/jhacc.$$
mkdir -p $TMPDIR

SPLITS=$TMPDIR/splits
ACCOUNTS=$TMPDIR/accounts
TRANS=$TMPDIR/entries
JOURNALS=$TMPDIR/journals


$SQLITE3 $DB "SELECT a.id, a.parentid, a.name, a.openingbalance, t.description FROM account a JOIN accounttype t ON a.accounttypeid=t.id" > $ACCOUNTS

$SQLITE3 $DB "SELECT s.id, s.accountid, s.amount, r.description FROM split s JOIN reconcilestate r ON s.reconcilestateid=r.id" > $SPLITS

$SQLITE3 $DB "SELECT t.id, t.num, t.date, t.payee, t.journalid FROM transentry t WHERE t.typeid=1" > $TRANS

$SQLITE3 $DB "SELECT j.id, j.name FROM journal j" > $JOURNALS

cat << END_TEXT
@prefix a: <http://com.ostrich-emulators/jfxhacc/account#> .
@prefix splits: <http://com.ostrich-emulators/jfxhacc/split/> .
@prefix payees: <http://com.ostrich-emulators/jfxhacc/payees/> .
@prefix xsd: <http://www.w3.org/2001/XMLSchema#> .
@prefix j: <http://com.ostrich-emulators/jfxhacc/journal#> .
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> .
@prefix p: <http://com.ostrich-emulators/jfxhacc/payee#> .
@prefix s: <http://com.ostrich-emulators/jfxhacc/split#> .
@prefix t: <http://com.ostrich-emulators/jfxhacc/transaction#> .
@prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .
@prefix jfxhacc: <http://com.ostrich-emulators/jfxhacc/> .
@prefix accounts: <http://com.ostrich-emulators/jfxhacc/accounts/> .
@prefix trans: <http://com.ostrich-emulators/jfxhacc/transaction/> .

<http://com.ostrich-emulators/jfxhacc#qhacc-export-$$> a jfxhacc:dataset .
END_TEXT


sed -e"s/\([^|]\).\(.*\)/j:qhacc-journal-\1 a jfxhacc:journal ; rdfs:label \"\2\" ./g" $JOURNALS

awk --field-separator \| '{printf("a:qhacc-account-%d a jfxhacc:account ; rdfs:label \"%s\" ; accounts:openingBalance \"%d\"^^xsd:int ; accounts:accountType jfxhacc:%s ",$1,$3,$4,tolower($5)); if( $2>0 ){ printf( "; accounts:parent a:qhacc-account-%d ", $2 ) } ; printf( ".\n" ) }' $ACCOUNTS

rm -rf $TMPDIR