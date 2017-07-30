#!/bin/sh
#
# Remove messages from the specified queue
#
# Parameters to be passed to this command
#  $1=Name of the queue manager
#  $2=Name of the queue name to clean
#
 
if [ $# -ne 2 ] ; then
#  Show help if required parameters are not passed to the script
   echo "Usage:"
   echo "   cleanq.sh QueueManagerName QueueName"
   exit;
fi
 
echo "clear qlocal($2)" | runmqsc $1
 
exit