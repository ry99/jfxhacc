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
SPLITTRANS=$TMPDIR/splittrans


$SQLITE3 $DB "SELECT a.id, a.parentid, a.name, a.openingbalance, t.description FROM account a JOIN accounttype t ON a.accounttypeid=t.id" > $ACCOUNTS

$SQLITE3 $DB "SELECT s.id, s.accountid, s.amount, s.memo, r.description FROM split s JOIN reconcilestate r ON s.reconcilestateid=r.id" > $SPLITS

$SQLITE3 $DB "SELECT t.id, t.num, t.date, t.payee, t.journalid FROM transentry t WHERE t.typeid=1" > $TRANS

$SQLITE3 $DB "SELECT x.transactionid, x.splitid FROM trans_split x JOIN transentry t ON x.transactionid=t.id JOIN split s ON x.splitid=s.id WHERE t.typeid=1" > $SPLITTRANS

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
@prefix dcterms: <http://purl.org/dc/terms/> .

<http://com.ostrich-emulators/jfxhacc#qhacc-export-$$> a jfxhacc:dataset .
END_TEXT

# make journals
sed -e"s/\([^|]\+\).\(.*\)/j:qhacc-journal-\1 a jfxhacc:journal ; rdfs:label \"\2\" ./g" $JOURNALS

# make accounts
sed -e's/"/\\"/g' $ACCOUNTS | awk --field-separator \| '{printf("a:qhacc-account-%d a jfxhacc:account ; rdfs:label \"%s\" ; accounts:openingBalance \"%d\"^^xsd:int ; accounts:accountType jfxhacc:%s ",$1,$3,$4,tolower($5)); if( $2>0 ){ printf( "; accounts:parent a:qhacc-account-%d ", $2 ) } ; printf( ".\n" ) }'

# make splits
cat $SPLITS | sed -e"s/Reconciled$/RECONCILED/g" \
  -e "s/No$/NOT_RECONCILED/g" \
  -e "s/Cleared$/CLEARED/g" \
  -e 's/"/\\"/g'| \
  awk --field-separator \| '{printf( "s:qhacc-split-%d a jfxhacc:split ; splits:account a:qhacc-account-%d ; splits:value \"%d\"^^xsd:int ; splits:reconciled \"%s\" ",$1,$2,$3,$5) ; if ( ""!=$4 ){ printf( "; splits:memo \"%s\" ",$4 ) }; printf( ".\n" ) }'

# make payees
declare -A PAYEES
while read line; do
  PAYEES["$line"]=${#PAYEES[@]}
  name=$(echo $line|sed -e's/"/\\"/g')
  echo "p:qhacc-payee-${PAYEES[$line]} a jfxhacc:payee ; rdfs:label \"$name\" ."
done < <(cat $TRANS | cut -d\| -f4|sort -u)

# make transactions
while read line; do
  echo "$line" | awk --field-separator \| \
  '{printf("t:qhacc-transaction-%d a jfxhacc:transaction ; trans:journal j:qhacc-journal-%s ; dcterms:created \"%sT00:00:00.000\"^^xsd:date ; ", $1,$5,$3); if( ""!=$2 ){ printf("trans:number \"%s\" ; ",$2) } }'
  
  payee=$(echo $line|cut -d\| -f4|sed -e 's/ *$//g'|sed -e 's/^ *//g' )
  id=${PAYEES[$payee]}
  echo " trans:payee p:qhacc-payee-$id ."
done < $TRANS

# link transactions and splits
sed -e"s/\([^|]\+\).\(.*\)/t:qhacc-transaction-\1 trans:entry s:qhacc-split-\2 ./g" $SPLITTRANS

rm -rf $TMPDIR