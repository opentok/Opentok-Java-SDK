#
# OpenTok Java SDK
# Copyright (C) 2025 Vonage.
# http://www.tokbox.com
#
# Licensed under The MIT License (MIT). See LICENSE file for more information.
#
if [ "$1" = "" ]
then
  echo "Usage: $0 <new version>"
  exit 1
fi

python3 -m pip install --upgrade pip
pip install bump2version
bump2version --new-version "$1" patch
