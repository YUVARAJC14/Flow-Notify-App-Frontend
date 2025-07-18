from setuptools import setup, find_packages

setup(
    name="flow_notify",
    version="0.1",
    packages=find_packages(),
    install_requires=[
        "APScheduler",
    ],
)