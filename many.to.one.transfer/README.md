# IBM MQ Managed File Transfer - Many to one file transfer using Ant script

## Introduction
This sample demonstrates how to transfer many files present at source end as one file at destination. This script also demonstrates how to run dependent transfers i.e, the second transfer runs only if the first transfer is successful. 

The script uses two transfers to transfer set of files source endpoint to a single file at destination endpoint. In the first step all files at source are transferred as messages to a queue. In the second step messagesfrom that queue are transferred as one file.

## Required Configuration
