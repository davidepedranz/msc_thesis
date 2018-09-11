#
# Copyright (c) 2018 Davide Pedranz. All rights reserved.
#
# This code is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program. If not, see <https://www.gnu.org/licenses/>.
#

import os


def mkdir(directory_path):
    """
    Create a directory at the given path (do nothing if it already exists.
    :param directory_path: Path of the directory to create.
    """
    if not os.path.exists(directory_path):
        os.makedirs(directory_path)


def exits(file_path):
    """
    Check if the given file exits.
    :param file_path: Path to the file.
    :return: True of the given path corresponds to a file, False otherwise.
    """
    return os.path.isfile(file_path)
