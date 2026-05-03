// ============================================================================
// SERVICE AUTHENTIFICATION - Gestion session utilisateur
// ============================================================================

import 'package:shared_preferences/shared_preferences.dart';
import '../models/forum_models.dart';
import 'api_service.dart';

class AuthService {
  static const String _tokenKey = 'auth_token';
  static const String _userKey = 'current_user';
  static const String _isLoggedInKey = 'is_logged_in';

  final ApiService api;
  User? _currentUser;
  String? _token;

  AuthService({ApiService? api}) : api = api ?? ApiService();

  // Getters
  User? get currentUser => _currentUser;
  bool get isAuthenticated => _token != null && _currentUser != null;
  bool get isAdmin => _currentUser?.isAdmin ?? false;
  String? get token => _token;

  // Initialiser depuis le stockage local
  Future<void> initialize() async {
    final prefs = await SharedPreferences.getInstance();
    _token = prefs.getString(_tokenKey);
    final userJson = prefs.getString(_userKey);

    if (userJson != null) {
      _currentUser = User.fromJson(jsonDecode(userJson));
    }
  }

  // Connexion
  Future<bool> login(String email, String password) async {
    try {
      final user = await api.login(email, password);
      if (user != null) {
        _currentUser = user;
        // Simuler un token (en production, le backend renvoie un JWT)
        _token = 'mock_token_${user.id}_${DateTime.now().millisecondsSinceEpoch}';

        await _saveSession();
        return true;
      }
    } catch (e) {
      print('Erreur AuthService.login: $e');
    }
    return false;
  }

  // Inscription
  Future<bool> register(String name, String email, String password) async {
    try {
      final user = await api.register(name, email, password);
      if (user != null) {
        _currentUser = user;
        _token = 'mock_token_${user.id}_${DateTime.now().millisecondsSinceEpoch}';

        await _saveSession();
        return true;
      }
    } catch (e) {
      print('Erreur AuthService.register: $e');
    }
    return false;
  }

  // Déconnexion
  Future<void> logout() async {
    _currentUser = null;
    _token = null;

    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_tokenKey);
    await prefs.remove(_userKey);
    await prefs.setBool(_isLoggedInKey, false);
  }

  // Sauvegarder session
  Future<void> _saveSession() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_tokenKey, _token!);
    await prefs.setString(_userKey, jsonEncode(_currentUser!.toJson()));
    await prefs.setBool(_isLoggedInKey, true);
  }

  // Vérifier si l'utilisateur est connecté
  Future<bool> isLoggedIn() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(_isLoggedInKey) ?? false;
  }

  // Récupérer l'utilisateur connecté
  Future<User?> getCurrentUser() async {
    if (_currentUser == null) {
      await initialize();
    }
    return _currentUser;
  }
}
