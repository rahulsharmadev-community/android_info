import 'dart:async';
import 'dart:developer' as developer;
import 'package:android_info/android_info.dart';
import 'package:flutter/material.dart';

void main() {
  runZonedGuarded(() {
    runApp(MaterialApp(
        theme: ThemeData(
            useMaterial3: true,
            colorSchemeSeed: const Color(0x9f4376f8),
            textTheme: const TextTheme(bodyMedium: TextStyle(fontSize: 13))),
        home: const MyApp()));
  }, (dynamic error, dynamic stack) {
    developer.log("Something went wrong!", error: error, stackTrace: stack);
  });
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  static final AndroidInfoPlugin deviceInfoPlugin = AndroidInfoPlugin();
  Map<String, dynamic> _deviceData = <String, dynamic>{};

  @override
  void initState() {
    super.initState();
    initPlatformState();
  }

  Future<void> initPlatformState() async {
    var deviceData = <String, dynamic>{};

    deviceData = (await deviceInfoPlugin.androidInfo).rawData;

    setState(() {
      _deviceData = deviceData;
    });
  }

  @override
  Widget build(BuildContext context) {
    print(_deviceData);
    return Scaffold(
      appBar: AppBar(
        title: const Text('Andoroid Device Info'),
        elevation: 4,
      ),
      body: ListView.separated(
        padding: const EdgeInsets.all(8),
        itemCount: _deviceData.entries.length,
        separatorBuilder: (context, index) => const Divider(),
        itemBuilder: (context, index) {
          final e = _deviceData.entries.elementAt(index);
          return Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: <Widget>[
                SizedBox(
                  width: 120,
                  child: Text(
                    e.key,
                    style: const TextStyle(
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
                Expanded(
                  child: buildValues(e.key, e.value),
                ),
              ]);
        },
      ),
    );
  }

  Widget buildValues(String title, dynamic element) {
    if (element is Map) {
      return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: (element)
              .entries
              .map((e) => Text('${e.key}: ${e.value}'))
              .toList());
    } else if (element is List) {
      return element.length > 50
          ? OutlinedButton(
              child: const Text('View'),
              onPressed: () {
                print('>>>>  onPressed');
                Navigator.push(
                    context,
                    MaterialPageRoute<void>(
                      builder: (context) => ListViewPage(
                        title: title,
                        list: element,
                      ),
                    ));
              },
            )
          : Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: element.map<Widget>((e) => Text('$e')).toList());
    } else {
      return Text(
        '$element',
        maxLines: 10,
        overflow: TextOverflow.ellipsis,
      );
    }
  }
}

class ListViewPage extends StatelessWidget {
  final String title;
  final List<dynamic> list;
  const ListViewPage({super.key, required this.title, required this.list});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: Text(title)),
      body: ListView.builder(
          padding: const EdgeInsets.all(8),
          itemCount: list.length,
          itemBuilder: (BuildContext context, int index) =>
              Text('${list[index]}')),
    );
  }
}
