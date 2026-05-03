// ============================================================================
// SERVICE API - Connexion base de données & opérations CRUD
// ============================================================================

import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/forum_models.dart';

class ApiService {
  // URL de base de l'API (à configurer selon votre backend)
  static const String baseUrl = 'http://localhost:8080/api';
  // Alternative: Utilisation de Firebase ou autre service cloud
  // static const String baseUrl = 'https://votre-app.firebaseio.com';

  final String? authToken;
  http.Client? _client;

  ApiService({this.authToken}) {
    _client = authToken != null
        ? http.Client()
        : null;
  }

  // ==================== AUTHENTIFICATION ====================

  Future<User?> login(String email, String password) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/auth/login'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({'email': email, 'password': password}),
      );

      if (response.statusCode == 200) {
        final data = jsonDecode(response.body);
        return User.fromJson(data['user']);
      }
    } catch (e) {
      print('Erreur login: $e');
    }
    return null;
  }

  Future<User?> register(String name, String email, String password) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/auth/register'),
        headers: {'Content-Type': 'application/json'},
        body: jsonEncode({
          'name': name,
          'email': email,
          'password': password,
          'role': 'ROLE_USER'
        }),
      );

      if (response.statusCode == 201) {
        final data = jsonDecode(response.body);
        return User.fromJson(data['user']);
      }
    } catch (e) {
      print('Erreur register: $e');
    }
    return null;
  }

  // ==================== CATÉGORIES ====================

  // Récupérer toutes les catégories
  Future<List<Category>> getCategories() async {
    try {
      final response = await _client?.get(
        Uri.parse('$baseUrl/categories'),
        headers: _headers,
      );

      if (response.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response.body);
        return data.map((json) => Category.fromJson(json)).toList();
      }
    } catch (e) {
      print('Erreur getCategories: $e');
    }
    return [];
  }

  // Récupérer une catégorie par ID
  Future<Category?> getCategory(int id) async {
    try {
      final response = await _client?.get(
        Uri.parse('$baseUrl/categories/$id'),
        headers: _headers,
      );

      if (response.statusCode == 200) {
        return Category.fromJson(jsonDecode(response.body));
      }
    } catch (e) {
      print('Erreur getCategory: $e');
    }
    return null;
  }

  // Créer une catégorie (ADMIN)
  Future<bool> createCategory(Category category) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/categories'),
        headers: _headers,
        body: jsonEncode(category.toJson()),
      );

      return response?.statusCode == 201;
    } catch (e) {
      print('Erreur createCategory: $e');
    }
    return false;
  }

  // Mettre à jour une catégorie (ADMIN)
  Future<bool> updateCategory(Category category) async {
    try {
      final response = await _client?.put(
        Uri.parse('$baseUrl/categories/${category.id}'),
        headers: _headers,
        body: jsonEncode(category.toJson()),
      );

      return response?.statusCode == 200;
    } catch (e) {
      print('Erreur updateCategory: $e');
    }
    return false;
  }

  // Supprimer une catégorie (ADMIN)
  Future<bool> deleteCategory(int id) async {
    try {
      final response = await _client?.delete(
        Uri.parse('$baseUrl/categories/$id'),
        headers: _headers,
      );

      return response?.statusCode == 204;
    } catch (e) {
      print('Erreur deleteCategory: $e');
    }
    return false;
  }

  // ==================== FORUMS ====================

  // Récupérer tous les forums
  Future<List<Forum>> getForums({int? categoryId, int? userId}) async {
    try {
      var queryParams = <String, String>{};
      if (categoryId != null) queryParams['category_id'] = categoryId.toString();
      if (userId != null) queryParams['user_id'] = userId.toString();

      final uri = Uri.parse('$baseUrl/forums').replace(queryParameters: queryParams);
      final response = await _client?.get(uri, headers: _headers);

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.map((json) => Forum.fromJson(json)).toList();
      }
    } catch (e) {
      print('Erreur getForums: $e');
    }
    return [];
  }

  // Récupérer un forum par ID
  Future<Forum?> getForum(int id) async {
    try {
      final response = await _client?.get(
        Uri.parse('$baseUrl/forums/$id'),
        headers: _headers,
      );

      if (response?.statusCode == 200) {
        return Forum.fromJson(jsonDecode(response!.body));
      }
    } catch (e) {
      print('Erreur getForum: $e');
    }
    return null;
  }

  // Créer un forum
  Future<Forum?> createForum(Forum forum) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/forums'),
        headers: _headers,
        body: jsonEncode(forum.toJson()),
      );

      if (response?.statusCode == 201) {
        return Forum.fromJson(jsonDecode(response!.body));
      }
    } catch (e) {
      print('Erreur createForum: $e');
    }
    return null;
  }

  // Mettre à jour un forum
  Future<bool> updateForum(Forum forum) async {
    try {
      final response = await _client?.put(
        Uri.parse('$baseUrl/forums/${forum.id}'),
        headers: _headers,
        body: jsonEncode(forum.toJson()),
      );

      return response?.statusCode == 200;
    } catch (e) {
      print('Erreur updateForum: $e');
    }
    return false;
  }

  // Supprimer un forum (avec suppression cascade des messages)
  Future<bool> deleteForum(int id) async {
    try {
      final response = await _client?.delete(
        Uri.parse('$baseUrl/forums/$id'),
        headers: _headers,
      );

      return response?.statusCode == 204;
    } catch (e) {
      print('Erreur deleteForum: $e');
    }
    return false;
  }

  // ==================== MESSAGES ====================

  // Récupérer les messages d'un forum
  Future<List<Message>> getForumMessages(int forumId, {int page = 1, int limit = 20}) async {
    try {
      final uri = Uri.parse('$baseUrl/forums/$forumId/messages')
          .replace(queryParameters: {'page': page.toString(), 'limit': limit.toString()});

      final response = await _client?.get(uri, headers: _headers);

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.map((json) => Message.fromJson(json)).toList();
      }
    } catch (e) {
      print('Erreur getForumMessages: $e');
    }
    return [];
  }

  // Créer un message
  Future<Message?> createMessage(Message message) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/messages'),
        headers: _headers,
        body: jsonEncode(message.toJson()),
      );

      if (response?.statusCode == 201) {
        return Message.fromJson(jsonDecode(response!.body));
      }
    } catch (e) {
      print('Erreur createMessage: $e');
    }
    return null;
  }

  // Mettre à jour un message
  Future<bool> updateMessage(Message message) async {
    try {
      final response = await _client?.put(
        Uri.parse('$baseUrl/messages/${message.id}'),
        headers: _headers,
        body: jsonEncode(message.toJson()),
      );

      return response?.statusCode == 200;
    } catch (e) {
      print('Erreur updateMessage: $e');
    }
    return false;
  }

  // Supprimer un message
  Future<bool> deleteMessage(int id) async {
    try {
      final response = await _client?.delete(
        Uri.parse('$baseUrl/messages/$id'),
        headers: _headers,
      );

      return response?.statusCode == 204;
    } catch (e) {
      print('Erreur deleteMessage: $e');
    }
    return false;
  }

  // Liker/unlike un message
  Future<bool> likeMessage(int messageId) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/messages/$messageId/like'),
        headers: _headers,
      );

      return response?.statusCode == 200;
    } catch (e) {
      print('Erreur likeMessage: $e');
    }
    return false;
  }

  // Disliker/unDislike un message
  Future<bool> dislikeMessage(int messageId) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/messages/$messageId/dislike'),
        headers: _headers,
      );

      return response?.statusCode == 200;
    } catch (e) {
      print('Erreur dislikeMessage: $e');
    }
    return false;
  }

  // ==================== STATISTIQUES (ADMIN) ====================

  Future<ForumStats> getForumStats() async {
    try {
      final response = await _client?.get(
        Uri.parse('$baseUrl/admin/forums/stats'),
        headers: _headers,
      );

      if (response?.statusCode == 200) {
        return ForumStats.fromJson(jsonDecode(response!.body));
      }
    } catch (e) {
      print('Erreur getForumStats: $e');
    }
    return ForumStats(
      totalCategories: 0,
      totalForums: 0,
      totalMessages: 0,
      activeForums: 0,
      inactiveForums: 0,
    );
  }

  // Récupérer top forums par nombre de messages
  Future<List<Map<String, dynamic>>> getTopForumsByMessages({int limit = 10}) async {
    try {
      final uri = Uri.parse('$baseUrl/admin/forums/top')
          .replace(queryParameters: {'limit': limit.toString()});

      final response = await _client?.get(uri, headers: _headers);

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.cast<Map<String, dynamic>>();
      }
    } catch (e) {
      print('Erreur getTopForumsByMessages: $e');
    }
    return [];
  }

  // Récupérer forums par catégorie
  Future<List<Map<String, dynamic>>> getForumsByCategory() async {
    try {
      final response = await _client?.get(
        Uri.parse('$baseUrl/admin/forums/by-category'),
        headers: _headers,
      );

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.cast<Map<String, dynamic>>();
      }
    } catch (e) {
      print('Erreur getForumsByCategory: $e');
    }
    return [];
  }

  // ==================== CLUSTERING (IA) ====================

  Future<List<Cluster>> runClustering({
    required String method,
    required int clusterCount,
  }) async {
    try {
      final response = await _client?.post(
        Uri.parse('$baseUrl/admin/forums/clustering'),
        headers: _headers,
        body: jsonEncode({
          'method': method,
          'cluster_count': clusterCount,
        }),
      );

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.map((json) => Cluster.fromJson(json)).toList();
      }
    } catch (e) {
      print('Erreur runClustering: $e');
    }
    return [];
  }

  // ==================== PRÉDICTIONS (IA) ====================

  Future<List<ForumPrediction>> getForumPredictions({int days = 30}) async {
    try {
      final uri = Uri.parse('$baseUrl/admin/forums/predictions')
          .replace(queryParameters: {'days': days.toString()});

      final response = await _client?.get(uri, headers: _headers);

      if (response?.statusCode == 200) {
        final List<dynamic> data = jsonDecode(response!.body);
        return data.map((json) => ForumPrediction.fromJson(json)).toList();
      }
    } catch (e) {
      print('Erreur getForumPredictions: $e');
    }
    return [];
  }

  Future<Map<String, dynamic>> getTrendsData({int days = 30}) async {
    try {
      final uri = Uri.parse('$baseUrl/admin/forums/trends')
          .replace(queryParameters: {'days': days.toString()});

      final response = await _client?.get(uri, headers: _headers);

      if (response?.statusCode == 200) {
        return jsonDecode(response!.body);
      }
    } catch (e) {
      print('Erreur getTrendsData: $e');
    }
    return {};
  }

  // ==================== UTILITAIRES ====================

  Map<String, String>? get _headers {
    return {
      'Content-Type': 'application/json',
      if (authToken != null) 'Authorization': 'Bearer $authToken',
    };
  }

  void dispose() {
    _client?.close();
  }
}
