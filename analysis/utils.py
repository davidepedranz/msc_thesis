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
